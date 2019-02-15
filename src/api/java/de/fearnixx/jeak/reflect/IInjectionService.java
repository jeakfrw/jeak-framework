package de.fearnixx.jeak.reflect;

/**
 * Injection service to run injections on own instances.
 */
public interface IInjectionService {

    <T> T injectInto(T victim);
}
