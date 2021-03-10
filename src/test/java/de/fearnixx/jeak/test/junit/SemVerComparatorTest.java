package de.fearnixx.jeak.test.junit;

import de.fearnixx.jeak.util.SemVerComparator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemVerComparatorTest {

    @Test
    public void testSameCompatible() {
        final var VERSION = "1.1.1";
        assertTrue(SemVerComparator.compare(VERSION, VERSION));
    }

    @Test
    public void testRaisedMinor() {
        final var V1 = "1.1.1";
        final var V2 = "1.2.2";
        assertTrue(SemVerComparator.compare(V1, V2));
    }

    @Test
    public void testRaisedBugfix() {
        final var V1 = "1.1.1";
        final var V2 = "1.1.2";
        assertTrue(SemVerComparator.compare(V1, V2));
    }

    @Test
    public void testRaisedMajor() {
        final var V1 = "1.1.1";
        final var V2 = "2.1.1";
        assertFalse(SemVerComparator.compare(V1, V2));
    }

    @Test
    public void testInsufficientMajor() {
        final var V1 = "2.1.1";
        final var V2 = "1.1.1";
        assertFalse(SemVerComparator.compare(V1, V2));
    }

    @Test
    public void testInsufficientMinor() {
        final var V1 = "1.2.1";
        final var V2 = "1.1.1";
        assertFalse(SemVerComparator.compare(V1, V2));
    }

    @Test
    public void testInsufficientBugfix() {
        final var V1 = "1.1.2";
        final var V2 = "1.1.1";
        assertFalse(SemVerComparator.compare(V1, V2));
    }

    @Test
    public void testSuffixIgnored() {
        final var V1 = "1.1.1";
        final var V2 = "1.1.1-SNAPSHOT";
        assertTrue(SemVerComparator.compare(V1, V2));
    }
}
