package de.fearnixx.t3.reflect;

/**
 * Created by MarkL4YG on 02-Feb-18
 */
public interface IInjectionService {

    void injectInto(Object victim);

    void injectInto(Object victim, String unitName);
}
