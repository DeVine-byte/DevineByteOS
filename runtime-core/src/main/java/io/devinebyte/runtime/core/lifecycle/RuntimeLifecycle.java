package io.devinebyte.runtime.core.lifecycle;

import jakarta.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class RuntimeLifecycle {
    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.CREATED);

    public LifecycleState getState() {
        return state.get();
    }

    public void transitionTo(LifecycleState newState) {
        state.set(newState);
    }
}
