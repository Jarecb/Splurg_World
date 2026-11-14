package org.jarec.gui;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LatestEdtUpdateTest {
    @Test
    void coalescesPendingUpdatesAndAppliesLatestValueOnEdt() throws Exception {
        AtomicReference<String> applied = new AtomicReference<>();
        AtomicBoolean ranOnEdt = new AtomicBoolean();
        LatestEdtUpdate<String> updates = new LatestEdtUpdate<>(value -> {
            ranOnEdt.set(SwingUtilities.isEventDispatchThread());
            applied.set(value);
        });

        for (int i = 0; i < 1_000; i++) {
            updates.submit("status-" + i);
        }
        SwingUtilities.invokeAndWait(() -> {});

        assertEquals("status-999", applied.get());
        assertTrue(ranOnEdt.get());
    }
}
