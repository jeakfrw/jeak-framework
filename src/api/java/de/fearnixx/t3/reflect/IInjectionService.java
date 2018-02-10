package de.fearnixx.t3.reflect;

/**
 * Created by MarkL4YG on 02-Feb-18
 */
public interface IInjectionService {

    <T> T injectInto(T victim);

    <T> T injectInto(T victim, String unitName);
}
