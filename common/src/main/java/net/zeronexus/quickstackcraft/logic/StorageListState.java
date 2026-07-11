package net.zeronexus.quickstackcraft.logic;

public enum StorageListState {
    DEFAULT,
    WHITELISTED,
    BLACKLISTED;

    public StorageListState next() {
        return switch (this) {
            case DEFAULT -> WHITELISTED;
            case WHITELISTED -> BLACKLISTED;
            case BLACKLISTED -> DEFAULT;
        };
    }
}
