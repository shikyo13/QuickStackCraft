package net.zeronexus.quickstackcraft.client.tutorial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TutorialScenesTest {

    @Test
    void chaptersFollowThePlayerWorkflowAndLeaveReadingTime() {
        var chapters = TutorialScenes.all();

        assertEquals(3, chapters.size());
        assertEquals(36.0D, chapters.get(0).durationSeconds());
        assertEquals(25.0D, chapters.get(1).durationSeconds());
        assertEquals(35.0D, chapters.get(2).durationSeconds());
    }
}
