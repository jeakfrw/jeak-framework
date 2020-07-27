package de.fearnixx.jeak.teamspeak.data;


/**
 * Functional interface class for property watchers.
 *
 * @param <K> the type of key that is used with the watched objects. Usually, this will be {@link String} but it could be anything suitable as a {@link java.util.Map} key.
 * @param <V> the type of value that is used with the watched property. Usually, this will be {@link Object} but can be specified further.
 */
@FunctionalInterface
public interface IPropertyWatcher<K, V> {

    /**
     * Notifies listener that the property has been changed.
     * Invocation of this method is solely for notification purposes and does not allow additional manipulation.
     * Directly invoking changes on the watched key is considered an anti-pattern as it potentially results in cyclic invocation.
     *
     * @param property For multipurpose-reasons, the property key will be passed to the listener. However, the listening side is <em>not</em> expected to be checking for the key if this instance of listener is registered only for one property.
     * @param oldValue The value that has been replaced or {@code null}.
     * @param presentValue The value that is now stored or {@code null}.
     *
     * @apiNote while most of the time a {@code null} value can be considered an addition/deletion, this information <em>is not part</em> of this contract. What a {@code null} value actually means is determined by the watched type.
     */
    void onValueChanged(K property, V oldValue, V presentValue);
}
