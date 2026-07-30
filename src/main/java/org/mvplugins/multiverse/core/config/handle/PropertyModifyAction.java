package org.mvplugins.multiverse.core.config.handle;

public enum PropertyModifyAction {
    SET(true),

    ADD(true),

    REMOVE(true),

    RESET(false),
    ;

    private final boolean requireValue;

    PropertyModifyAction(boolean requireValue) {
        this.requireValue = requireValue;
    }

    public boolean isRequireValue() {
        return requireValue;
    }
}
