package net.zeronexus.quickstackcraft.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Assigns one available choice to every required slot without depending on display order.
 */
public final class ChoiceAllocator {

    private ChoiceAllocator() {}

    public static <K> Allocation<K> allocate(List<? extends List<K>> options,
                                             Map<K, Integer> available,
                                             int amountPerSlot) {
        if (amountPerSlot <= 0) {
            throw new IllegalArgumentException("amountPerSlot must be positive");
        }
        if (options.isEmpty()) {
            return new Allocation<>(true, List.of());
        }

        List<List<K>> normalized = new ArrayList<>(options.size());
        for (List<K> slotOptions : options) {
            LinkedHashSet<K> unique = new LinkedHashSet<>();
            for (K option : slotOptions) {
                if (option != null) {
                    unique.add(option);
                }
            }
            if (unique.isEmpty()) {
                return new Allocation<>(false, List.of());
            }
            normalized.add(new ArrayList<>(unique));
        }

        List<Integer> order = new ArrayList<>(normalized.size());
        for (int i = 0; i < normalized.size(); i++) {
            order.add(i);
        }
        order.sort(Comparator
                .comparingInt((Integer slot) -> viableChoiceCount(
                        normalized.get(slot), available, amountPerSlot))
                .thenComparingInt(Integer::intValue));

        Map<K, List<Integer>> unitsByChoice = createCapacityUnits(
                normalized, available, amountPerSlot);
        int unitCount = unitsByChoice.values().stream().mapToInt(List::size).sum();
        int[] unitOwners = new int[unitCount];
        Arrays.fill(unitOwners, -1);

        List<K> selected = new ArrayList<>(Collections.nCopies(normalized.size(), null));
        for (int slot : order) {
            if (!assign(slot, normalized, unitsByChoice, unitOwners,
                    new boolean[unitCount], selected)) {
                return new Allocation<>(false, List.of());
            }
        }
        return new Allocation<>(true, selected);
    }

    private static <K> Map<K, List<Integer>> createCapacityUnits(List<List<K>> options,
                                                                 Map<K, Integer> available,
                                                                 int amountPerSlot) {
        Map<K, List<Integer>> unitsByChoice = new LinkedHashMap<>();
        int nextUnit = 0;
        for (List<K> slotOptions : options) {
            for (K option : slotOptions) {
                if (unitsByChoice.containsKey(option)) {
                    continue;
                }
                int capacity = Math.min(options.size(),
                        Math.max(0, available.getOrDefault(option, 0) / amountPerSlot));
                List<Integer> units = new ArrayList<>(capacity);
                for (int i = 0; i < capacity; i++) {
                    units.add(nextUnit++);
                }
                unitsByChoice.put(option, units);
            }
        }
        return unitsByChoice;
    }

    private static <K> boolean assign(int slot, List<List<K>> options,
                                      Map<K, List<Integer>> unitsByChoice,
                                      int[] unitOwners, boolean[] visitedUnits,
                                      List<K> selected) {
        for (K option : options.get(slot)) {
            for (int unit : unitsByChoice.getOrDefault(option, List.of())) {
                if (visitedUnits[unit]) {
                    continue;
                }
                visitedUnits[unit] = true;

                int previousOwner = unitOwners[unit];
                if (previousOwner == -1 || assign(previousOwner, options, unitsByChoice,
                        unitOwners, visitedUnits, selected)) {
                    unitOwners[unit] = slot;
                    selected.set(slot, option);
                    return true;
                }
            }
        }
        return false;
    }

    private static <K> int viableChoiceCount(List<K> options, Map<K, Integer> available,
                                             int amountPerSlot) {
        int count = 0;
        for (K option : options) {
            if (available.getOrDefault(option, 0) >= amountPerSlot) {
                count++;
            }
        }
        return count;
    }

    public record Allocation<K>(boolean complete, List<K> selected) {
        public Allocation {
            selected = Collections.unmodifiableList(new ArrayList<>(selected));
        }
    }
}
