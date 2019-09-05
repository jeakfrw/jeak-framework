package de.fearnixx.jeak.teamspeak.data;

/**
 * Expansion interface for {@link IDataHolder} contracts that additionally support listening for changes in specific properties.
 */
public interface IWatchableDataHolder {

    /**
     * Registers a new listener on the given property.
     * The listener will be notified about changes to this property, in accordance to {@link IPropertyWatcher}.
     *
     * @param propertyName the property key to listen to.
     * @param listener the actual listener. (May be a lambda-expression as {@link IPropertyWatcher} is an annotated {@link FunctionalInterface}.
     */
    void watch(String propertyName, IPropertyWatcher<String, String> listener);
}
