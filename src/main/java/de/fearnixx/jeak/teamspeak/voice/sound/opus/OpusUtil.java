package de.fearnixx.jeak.teamspeak.voice.sound.opus;


import net.tomp2p.opuswrapper.Opus;

/**
 * This class was extracted from an example by manevolent
 */
public class OpusUtil {
    public static int checkError(String description, int returnCode) throws RuntimeException {
        if (returnCode < Opus.OPUS_OK)
            throw new RuntimeException("Opus error [" + description + "] (" + returnCode + "): " +
                    Opus.INSTANCE.opus_strerror(returnCode).trim()
            );
        else return returnCode;
    }

    public static int checkError(int returnCode) throws RuntimeException {
        if (returnCode < Opus.OPUS_OK)
            throw new RuntimeException(
                    "Opus error (" + returnCode + "): " +
                            Opus.INSTANCE.opus_strerror(returnCode).trim()
            );
        else return returnCode;
    }

    public static String getVersion() {
        return Opus.INSTANCE.opus_get_version_string();
    }
}
