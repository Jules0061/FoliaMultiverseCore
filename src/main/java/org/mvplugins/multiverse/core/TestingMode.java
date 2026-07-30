package org.mvplugins.multiverse.core;

final class TestingMode {

    private static boolean enabled = false;

    private TestingMode() {
    }

    static void enable() {
        enabled = true;
    }

    static boolean isDisabled() {
        return !enabled;
    }
}
