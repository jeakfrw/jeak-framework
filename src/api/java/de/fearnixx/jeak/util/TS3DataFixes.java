package de.fearnixx.jeak.util;

import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;

import java.util.Optional;

/**
 * Data inconsistencies in the query responses may be worked-around in the framework.
 * This class contains a selection of those fixes.
 *
 * <p>Not included are semantic issues like:
 * * {@link de.fearnixx.jeak.event.IQueryEvent.INotification.IClientEnter}
 * using "ctid" instead of "cid" for the channel ID. (Fix not included in framework.)
 * * {@link de.fearnixx.jeak.event.IQueryEvent.INotification.IChannelEdited}s information
 * being missing for edited descriptions and password. (Fix included in framework.)
 */
public class TS3DataFixes {

    /**
     * It appears that TS3 tends to fail reading icon IDs correctly.
     * Icon IDs are supposed to be a CRC32 checksum. However, TS3 very frequently returns negative numbers for those.
     * Investigation showed TS3 reading the internal integer as signed whereas it really is unsigned.
     *
     * <p>This will fix this issue by re-reading negative icon IDs as unsigned numbers.
     *
     * @param dataHolder   The {@link IDataHolder} this fix should be applied to.
     * @param propertyName The property under which the icon ID is found.
     * @see PropertyKeys.Client#ICON_ID
     * @see PropertyKeys.Channel#ICON_ID
     */
    public static void iconsInvalidCrc32(IDataHolder dataHolder, String propertyName) {
        Optional<String> optIconID = dataHolder.getProperty(propertyName);
        optIconID.ifPresent(iconId -> {
            if (iconId.startsWith("-")) {
                int idFromTS = Integer.parseInt(iconId);
                long realID = Integer.toUnsignedLong(idFromTS);
                dataHolder.setProperty(
                        PropertyKeys.Channel.ICON_ID,
                        Long.toString(realID)
                );
            }
        });
    }
}
