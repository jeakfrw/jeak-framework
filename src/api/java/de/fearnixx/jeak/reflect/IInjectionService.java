package de.fearnixx.jeak.reflect;

/**
 * Injection service to run injections on own instances.
 */
public interface IInjectionService {

    /**
     * Applies injections to a given object.
     * As we do not proxy objects and do not self-invoke on objects other than plugin instances,
     * plugins and services must manually invoke injections for any of their own objects.
     */
    <T> T injectInto(T victim);
}
