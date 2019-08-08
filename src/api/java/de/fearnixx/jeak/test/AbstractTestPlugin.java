package de.fearnixx.jeak.test;

import java.util.*;

/**
 * As JUnit-testing is not compatible with high-level testing for the framework without having to mock the major part,
 * an alternative solution is available:
 * Test plugins are plugins that extend this abstract class.
 * The class tracks the instances created while the framework is running.
 * This way, the framework can be connected to a real test instance of TS3 in order to run tests.
 *
 * <p>On shutdown, the framework then retrieves the registered tests and their results.
 */
public abstract class AbstractTestPlugin {

    private static final List<AbstractTestPlugin> testPlugins = new LinkedList<>();

    public static List<AbstractTestPlugin> getTestPlugins() {
        return Collections.unmodifiableList(testPlugins);
    }

    private final Map<String, Boolean> results = new HashMap<>();

    protected AbstractTestPlugin() {
        testPlugins.add(this);
    }

    protected void addTest(String testName) {
        results.put(testName, false);
    }

    protected void fail(String testName) {
        results.put(testName, false);
    }

    protected void success(String testName) {
        results.put(testName, true);
    }

    public Map<String, Boolean> getResults() {
        return results;
    }
}
