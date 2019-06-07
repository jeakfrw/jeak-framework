package de.fearnixx.jeak.service.permission.base;

/**
 * Representation of a generic permission entry.
 */
public interface IPermission {

    /**
     * The string ID of the permission
     * Examples:
     * <ul>
     *     <li>{@code "i_channel_join_power"}</li>
     *     <li>{@code "afkutils.ignore_afk"}</li>
     * </ul>
     */
    String getSID();

    /**
     * The ID of the permission where values originate from
     * Example:
     * <ul>
     *     <li>{@code "teamspeak"}</li>
     *     <li>{@code "jeak"}</li>
     * </ul>
     */
    String getSystemID();

    /**
     * Fully qualified ID. In form:
     *   "\<system\>:\<sid\>"
     * Examples:
     * <ul>
     *     <li>{@code "teamspeak:i_channel_join_power"}</li>
     *     <li>{@code "jeak:afkutils.ignore_afk"}</li>
     * </ul>
     */
    String getFullyQualifiedID();

    /**
     * The actual value of the permission.
     */
    Integer getValue();
}
