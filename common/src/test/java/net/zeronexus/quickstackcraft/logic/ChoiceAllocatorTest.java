package net.zeronexus.quickstackcraft.logic;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChoiceAllocatorTest {

    @Test
    void reservesSharedChoiceForTheConstrainedSlot() {
        Map<String, Integer> available = new LinkedHashMap<>();
        available.put("oak", 1);
        available.put("birch", 1);

        ChoiceAllocator.Allocation<String> result = ChoiceAllocator.allocate(
                List.of(List.of("oak", "birch"), List.of("oak")), available, 1);

        assertTrue(result.complete());
        assertEquals(List.of("birch", "oak"), result.selected());
        assertEquals(Map.of("oak", 1, "birch", 1), available);
    }

    @Test
    void accountsForTheRequestedAmountInEverySlot() {
        ChoiceAllocator.Allocation<String> result = ChoiceAllocator.allocate(
                List.of(List.of("plank"), List.of("plank")), Map.of("plank", 6), 3);

        assertTrue(result.complete());
        assertEquals(List.of("plank", "plank"), result.selected());
    }

    @Test
    void rejectsAnAssignmentThatOnlyHasEnoughForOneSlot() {
        ChoiceAllocator.Allocation<String> result = ChoiceAllocator.allocate(
                List.of(List.of("plank"), List.of("plank")), Map.of("plank", 5), 3);

        assertFalse(result.complete());
        assertTrue(result.selected().isEmpty());
    }
}
