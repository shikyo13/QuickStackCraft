package net.zeronexus.quickstackcraft.client.tutorial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TutorialTimelineTest {

    @Test
    void transitionClampsAndEases() {
        assertEquals(0.0D, TutorialTimeline.transition(1.0D, 2.0D, 4.0D));
        assertEquals(0.5D, TutorialTimeline.transition(3.0D, 2.0D, 4.0D), 0.0001D);
        assertEquals(1.0D, TutorialTimeline.transition(5.0D, 2.0D, 4.0D));
    }

    @Test
    void cubicBezierPreservesEndpointsAndCurvesThroughControls() {
        assertEquals(2.0D, TutorialTimeline.cubicBezier(2.0D, 4.0D, 8.0D, 10.0D, 0.0D));
        assertEquals(6.0D, TutorialTimeline.cubicBezier(2.0D, 4.0D, 8.0D, 10.0D, 0.5D),
                0.0001D);
        assertEquals(10.0D, TutorialTimeline.cubicBezier(2.0D, 4.0D, 8.0D, 10.0D, 1.0D));
    }

    @Test
    void playbackAdvancesPausesAndReplaysDeterministically() {
        TutorialPlaybackController playback = new TutorialPlaybackController(
                new double[] {2.0D, 3.0D}, 0);

        playback.advance(2.5D);
        assertEquals(1, playback.sceneIndex());
        assertEquals(0.5D, playback.elapsedSeconds(), 0.0001D);

        playback.togglePaused();
        assertTrue(playback.paused());
        playback.replay();
        assertFalse(playback.paused());
        assertEquals(0.0D, playback.elapsedSeconds(), 0.0001D);

        playback.advance(4.0D);
        assertTrue(playback.paused());
        assertEquals(1.0D, playback.progress(), 0.0001D);

        playback.replay();
        playback.advance(0.5D);
        playback.next();
        assertEquals(1, playback.sceneIndex());
        assertEquals(0.5D, playback.elapsedSeconds(), 0.0001D);
    }
}
