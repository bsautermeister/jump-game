package de.bsautermeister.jump.sprites;

public class GameObjectState<T extends Enum<T>> {
    private T current;
    private T previous;
    private float stateTimer;

    public GameObjectState(T initialState) {
        current = initialState;
        previous = initialState;
        resetTimer();
    }

    public void upate(float delta) {
        stateTimer += delta;
    }

    public void set(T state) {
        if (current == state) {
            return;
        }

        previous = current;
        current = state;
        resetTimer();
    }

    public boolean is(T state) {
        return current == state;
    }

    public boolean was(T state) {
        return previous == state;
    }

    public void resetTimer() {
        stateTimer = 0;
    }

    public boolean changed() {
        return current != previous;
    }

    public T current() {
        return current;
    }

    public T previous() {
        return previous;
    }

    public float timer() {
        return stateTimer;
    }
}
