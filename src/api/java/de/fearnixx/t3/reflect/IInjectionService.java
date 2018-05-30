package de.fearnixx.t3.reflect;

/**
 * Injection service to run injections on own instances.
 */
public interface IInjectionService {

    <T> T injectInto(T victim);

    <T> T injectInto(T victim, String unitName);
}
