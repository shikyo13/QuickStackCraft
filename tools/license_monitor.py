#!/usr/bin/env python3
"""Detect changes in a monitored repository and surface possible source reuse."""

from __future__ import annotations

import argparse
import hashlib
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import PurePosixPath


REMOTE_NAME = "license-monitor"
TEXT_EXTENSIONS = {".java", ".json", ".toml", ".gradle", ".properties"}
BINARY_EXTENSIONS = {".gif", ".jpeg", ".jpg", ".ogg", ".png", ".webp"}
IGNORED_PARTS = {
    ".git",
    ".gradle",
    "build",
    "generated",
    "media",
    "release-media",
    "run",
}
JAVA_KEYWORDS = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch",
    "char", "class", "const", "continue", "default", "do", "double",
    "else", "enum", "extends", "final", "finally", "float", "for",
    "goto", "if", "implements", "import", "instanceof", "int",
    "interface", "long", "native", "new", "non-sealed", "null", "package",
    "permits", "private", "protected", "public", "record", "return",
    "sealed", "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "true", "try",
    "var", "void", "volatile", "while", "yield", "false",
}
TOKEN_PATTERN = re.compile(
    r"(?P<comment>//[^\n]*|/\*.*?\*/)"
    r"|(?P<string>\"(?:\\.|[^\"\\])*\"|'(?:\\.|[^'\\])*')"
    r"|(?P<number>\b(?:0[xX][0-9a-fA-F_]+|\d[\d_]*(?:\.\d[\d_]*)?)\b)"
    r"|(?P<identifier>[A-Za-z_$][A-Za-z0-9_$]*)"
    r"|(?P<operator>>>?=?|<<=?|::|->|==|!=|<=|>=|&&|\|\||\+\+|--|"
    r"\+=|-=|\*=|/=|%=|&=|\|=|\^=|[{}()\[\].,;:+\-*/%&|^!~<>=?@])",
    re.DOTALL,
)


@dataclass(frozen=True)
class Token:
    exact: str
    normalized: str
    line: int


@dataclass(frozen=True)
class Location:
    path: str
    line: int
    reference: str


@dataclass(frozen=True)
class Finding:
    kind: str
    branch: str
    commit: str
    candidate: Location
    upstream: Location
    detail: str


def run_git(*args: str, text: bool = True, check: bool = True) -> str | bytes:
    completed = subprocess.run(
        ["git", *args],
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=text,
    )
    if check and completed.returncode != 0:
        stderr = completed.stderr.strip() if text else completed.stderr.decode(errors="replace").strip()
        raise RuntimeError(f"git {' '.join(args)} failed: {stderr}")
    return completed.stdout


def parse_mapping(values: list[str], option: str) -> dict[str, str]:
    mappings: dict[str, str] = {}
    for value in values:
        if "=" not in value:
            raise ValueError(f"{option} must use branch=value syntax: {value}")
        branch, target = value.split("=", 1)
        if not branch or not target:
            raise ValueError(f"{option} has an empty branch or value: {value}")
        mappings[branch] = target
    return mappings


def is_tracked_content(path: str) -> bool:
    parsed = PurePosixPath(path)
    return not any(part in IGNORED_PARTS for part in parsed.parts)


def list_files(reference: str, extensions: set[str]) -> list[str]:
    output = run_git("ls-tree", "-r", "--name-only", reference)
    return [
        path
        for path in output.splitlines()
        if is_tracked_content(path) and PurePosixPath(path).suffix.lower() in extensions
    ]


def read_blob(reference: str, path: str) -> bytes | None:
    completed = subprocess.run(
        ["git", "show", f"{reference}:{path}"],
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
    )
    return completed.stdout if completed.returncode == 0 else None


def tokenize(data: bytes) -> list[Token]:
    source = data.decode("utf-8", errors="replace")
    source = re.sub(r"(?m)^\s*(?:package|import)\s+[^;]+;\s*$", "", source)
    tokens: list[Token] = []
    line = 1
    cursor = 0
    for match in TOKEN_PATTERN.finditer(source):
        line += source.count("\n", cursor, match.start())
        cursor = match.end()
        if match.lastgroup == "comment":
            line += match.group(0).count("\n")
            continue
        value = match.group(0)
        if match.lastgroup == "identifier":
            normalized = value if value in JAVA_KEYWORDS else "ID"
        elif match.lastgroup == "string":
            normalized = "STR"
        elif match.lastgroup == "number":
            normalized = "NUM"
        else:
            normalized = value
        tokens.append(Token(value, normalized, line))
        line += value.count("\n")
    return tokens


def digest_window(values: list[str]) -> str:
    return hashlib.sha256("\x1f".join(values).encode("utf-8")).hexdigest()


def token_fingerprints(tokens: list[Token], width: int, normalized: bool) -> list[tuple[str, int]]:
    if len(tokens) < width:
        return []
    values = [token.normalized if normalized else token.exact for token in tokens]
    return [
        (digest_window(values[index:index + width]), tokens[index].line)
        for index in range(len(tokens) - width + 1)
    ]


def text_phrases(tokens: list[Token]) -> list[tuple[str, int]]:
    phrases: list[tuple[str, int]] = []
    for token in tokens:
        if not token.exact.startswith(('"', "'")):
            continue
        phrase = token.exact[1:-1].replace("\\n", " ").strip()
        if len(phrase) >= 28 and len(phrase.split()) >= 4:
            phrases.append((phrase, token.line))
    return phrases


def build_baseline(reference: str) -> tuple[set[str], set[str], set[str], set[str]]:
    exact: set[str] = set()
    structural: set[str] = set()
    phrases: set[str] = set()
    assets: set[str] = set()
    for path in list_files(reference, TEXT_EXTENSIONS):
        data = read_blob(reference, path)
        if data is None:
            continue
        tokens = tokenize(data)
        exact.update(key for key, _ in token_fingerprints(tokens, 32, False))
        if PurePosixPath(path).suffix.lower() == ".java":
            structural.update(key for key, _ in token_fingerprints(tokens, 96, True))
        phrases.update(phrase for phrase, _ in text_phrases(tokens))
    for path in list_files(reference, BINARY_EXTENSIONS):
        data = read_blob(reference, path)
        if data is not None:
            assets.add(hashlib.sha256(data).hexdigest())
    return exact, structural, phrases, assets


def build_upstream_index(
    references: list[str],
    baseline: tuple[set[str], set[str], set[str], set[str]],
) -> tuple[dict[str, Location], dict[str, Location], dict[str, Location], dict[str, Location]]:
    baseline_exact, baseline_structural, baseline_phrases, baseline_assets = baseline
    exact: dict[str, Location] = {}
    structural: dict[str, Location] = {}
    phrases: dict[str, Location] = {}
    assets: dict[str, Location] = {}
    for reference in references:
        for path in list_files(reference, TEXT_EXTENSIONS):
            data = read_blob(reference, path)
            if data is None:
                continue
            tokens = tokenize(data)
            for key, line in token_fingerprints(tokens, 32, False):
                if key not in baseline_exact:
                    exact.setdefault(key, Location(path, line, reference))
            if PurePosixPath(path).suffix.lower() == ".java":
                for key, line in token_fingerprints(tokens, 96, True):
                    if key not in baseline_structural:
                        structural.setdefault(key, Location(path, line, reference))
            for phrase, line in text_phrases(tokens):
                if phrase not in baseline_phrases:
                    phrases.setdefault(phrase, Location(path, line, reference))
        for path in list_files(reference, BINARY_EXTENSIONS):
            data = read_blob(reference, path)
            if data is None:
                continue
            digest = hashlib.sha256(data).hexdigest()
            if digest not in baseline_assets:
                assets.setdefault(digest, Location(path, 1, reference))
    return exact, structural, phrases, assets


def changed_files(commit: str) -> list[str]:
    output = run_git("diff-tree", "--root", "--no-commit-id", "--name-only", "-r", commit)
    return [path for path in output.splitlines() if is_tracked_content(path)]


def inspect_commit(
    branch: str,
    commit: str,
    baseline: tuple[set[str], set[str], set[str], set[str]],
    upstream: tuple[dict[str, Location], dict[str, Location], dict[str, Location], dict[str, Location]],
) -> list[Finding]:
    baseline_exact, baseline_structural, baseline_phrases, baseline_assets = baseline
    upstream_exact, upstream_structural, upstream_phrases, upstream_assets = upstream
    findings: list[Finding] = []
    seen: set[tuple[str, str, str]] = set()
    for path in changed_files(commit):
        extension = PurePosixPath(path).suffix.lower()
        data = read_blob(commit, path)
        if data is None:
            continue
        candidate = Location(path, 1, commit)
        if extension in BINARY_EXTENSIONS:
            digest = hashlib.sha256(data).hexdigest()
            match = upstream_assets.get(digest) if digest not in baseline_assets else None
            if match:
                findings.append(Finding("asset", branch, commit, candidate, match, "identical binary asset"))
            continue
        if extension not in TEXT_EXTENSIONS:
            continue
        tokens = tokenize(data)
        checks = [
            ("exact", token_fingerprints(tokens, 32, False), baseline_exact, upstream_exact, "32-token exact match"),
            ("wording", text_phrases(tokens), baseline_phrases, upstream_phrases, "distinctive user-facing text match"),
        ]
        if extension == ".java":
            checks.append((
                "structural",
                token_fingerprints(tokens, 96, True),
                baseline_structural,
                upstream_structural,
                "96-token normalized structure match"))
        for kind, candidates, baseline_keys, upstream_index, detail in checks:
            for key, line in candidates:
                match = upstream_index.get(key) if key not in baseline_keys else None
                if not match:
                    continue
                dedupe = (kind, path, match.path)
                if dedupe in seen:
                    continue
                seen.add(dedupe)
                findings.append(
                    Finding(kind, branch, commit, Location(path, line, commit), match, detail)
                )
    return findings


def is_ancestor(ancestor: str, descendant: str) -> bool:
    result = subprocess.run(
        ["git", "merge-base", "--is-ancestor", ancestor, descendant],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    return result.returncode == 0


def monitored_branches() -> dict[str, str]:
    output = run_git(
        "for-each-ref",
        f"refs/remotes/{REMOTE_NAME}",
        "--format=%(refname:short) %(objectname)",
    )
    branches: dict[str, str] = {}
    for line in output.splitlines():
        reference, commit = line.split(maxsplit=1)
        branch = reference.removeprefix(f"{REMOTE_NAME}/")
        if branch != "HEAD":
            branches[branch] = commit
    return branches


def choose_baseline(branch: str, head: str, baselines: dict[str, str]) -> tuple[str | None, bool]:
    direct = baselines.get(branch)
    if direct:
        return direct, is_ancestor(direct, head)
    ancestors = [commit for commit in baselines.values() if is_ancestor(commit, head)]
    return (ancestors[0], True) if ancestors else (None, False)


def matching_upstream(branch: str, upstream: dict[str, str]) -> list[str]:
    direct = upstream.get(branch)
    return [direct] if direct else list(dict.fromkeys(upstream.values()))


def write_report(
    path: str | None,
    branch_changes: list[tuple[str, str, list[str], bool]],
    findings: list[Finding],
) -> None:
    lines = ["# Monitored repository review", ""]
    if not branch_changes:
        lines.append("No monitored branch has advanced beyond its approved baseline.")
    else:
        lines.append("New or rewritten branch history requires owner review.")
        lines.append("")
        lines.append("| Branch | Head | New commits | History intact |")
        lines.append("|-|-|-|-|")
        for branch, head, commits, intact in branch_changes:
            lines.append(f"| `{branch}` | `{head[:12]}` | {len(commits)} | {'Yes' if intact else 'No'} |")
    lines.extend(["", "## Similarity findings", ""])
    if not findings:
        lines.append("No protected exact, structural, wording, or binary-asset match was detected.")
    else:
        lines.append("These are review signals, not an automatic legal conclusion.")
        lines.append("")
        lines.append("| Kind | Monitored location | Upstream location | Detail |")
        lines.append("|-|-|-|-|")
        for finding in findings[:50]:
            candidate = f"{finding.candidate.path}:{finding.candidate.line} @ {finding.commit[:12]}"
            source = f"{finding.upstream.path}:{finding.upstream.line} @ {finding.upstream.reference}"
            lines.append(f"| {finding.kind} | `{candidate}` | `{source}` | {finding.detail} |")
        if len(findings) > 50:
            lines.append(f"\nOnly the first 50 of {len(findings)} findings are shown.")
    report = "\n".join(lines) + "\n"
    print(report)
    if path:
        with open(path, "a", encoding="utf-8") as summary:
            summary.write(report)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repository", required=True)
    parser.add_argument("--baseline", action="append", default=[], required=True)
    parser.add_argument("--upstream", action="append", default=[], required=True)
    parser.add_argument("--summary")
    args = parser.parse_args()

    baselines = parse_mapping(args.baseline, "--baseline")
    upstream = parse_mapping(args.upstream, "--upstream")
    run_git("fetch", "--no-tags", "origin", "+refs/heads/mc/*:refs/remotes/origin/mc/*")
    existing = run_git("remote", "get-url", REMOTE_NAME, check=False).strip()
    if existing:
        run_git("remote", "set-url", REMOTE_NAME, args.repository)
    else:
        run_git("remote", "add", REMOTE_NAME, args.repository)
    run_git("fetch", "--prune", "--no-tags", REMOTE_NAME, f"+refs/heads/*:refs/remotes/{REMOTE_NAME}/*")

    branch_changes: list[tuple[str, str, list[str], bool]] = []
    findings: list[Finding] = []
    for branch, head in monitored_branches().items():
        baseline_ref, intact = choose_baseline(branch, head, baselines)
        if baseline_ref == head:
            continue
        if baseline_ref and intact:
            commits = run_git("rev-list", "--reverse", f"{baseline_ref}..{head}").splitlines()
            baseline = build_baseline(baseline_ref)
        else:
            commits = [head]
            baseline = (set(), set(), set(), set())
        branch_changes.append((branch, head, commits, intact))
        source_index = build_upstream_index(matching_upstream(branch, upstream), baseline)
        for commit in commits:
            findings.extend(inspect_commit(branch, commit, baseline, source_index))

    write_report(args.summary, branch_changes, findings)
    if findings:
        print("Potential protected-source overlap requires manual review.", file=sys.stderr)
        return 2
    if branch_changes:
        print("The monitored repository changed and requires manual review.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (RuntimeError, ValueError) as error:
        print(f"Monitor failed: {error}", file=sys.stderr)
        raise SystemExit(3)
