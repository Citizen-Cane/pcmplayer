package pcm.controller;

import java.util.function.BooleanSupplier;

import pcm.model.Action;

public class Assertion extends LambdaTrigger {

    public Assertion(String message, Action action, BooleanSupplier condition) {
        super(action, () -> {
            if (!condition.getAsBoolean()) {
                throw new IllegalStateException(message + " expected");
            }
        });
    }

}
