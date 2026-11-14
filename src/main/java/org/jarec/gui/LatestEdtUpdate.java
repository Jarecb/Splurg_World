package org.jarec.gui;

import javax.swing.SwingUtilities;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class LatestEdtUpdate<T> {
    private final AtomicReference<T> pending = new AtomicReference<>();
    private final AtomicBoolean queued = new AtomicBoolean();
    private final Consumer<T> update;

    LatestEdtUpdate(Consumer<T> update) {
        this.update = Objects.requireNonNull(update);
    }

    void submit(T value) {
        pending.set(Objects.requireNonNull(value));
        queueUpdate();
    }

    private void queueUpdate() {
        if (!queued.compareAndSet(false, true)) return;
        SwingUtilities.invokeLater(this::applyLatest);
    }

    private void applyLatest() {
        try {
            T value = pending.getAndSet(null);
            if (value != null) update.accept(value);
        } finally {
            queued.set(false);
            if (pending.get() != null) queueUpdate();
        }
    }
}
