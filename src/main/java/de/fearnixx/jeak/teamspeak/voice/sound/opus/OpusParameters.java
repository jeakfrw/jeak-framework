package de.fearnixx.jeak.teamspeak.voice.sound.opus;

/**
 * This class was extracted from an example by manevolent.
 * <p>
 * URL: https://github.com/Manevolent/ts3j/blob/9d602a8f98480c2c434fa1b7c6b9b0ae893f967f/examples/audio/src/main/java/com/github/manevolent/ts3j/examples/audio/OpusParameters.java
 */
public class OpusParameters {
    private final int opusFrameRate;
    private final int opusBitrate;
    private final int opusComplexity;
    private final int opusPacketLoss;
    private final boolean opusVbr;
    private final boolean opusFec;
    private final boolean opusMusic;

    public OpusParameters(int opusFrameRate, int opusBitrate,
                          int opusComplexity, int opusPacketLoss,
                          boolean opusVbr, boolean opusFec, boolean opusMusic) {
        this.opusFrameRate = opusFrameRate;
        this.opusBitrate = opusBitrate;
        this.opusComplexity = opusComplexity;
        this.opusPacketLoss = opusPacketLoss;
        this.opusVbr = opusVbr;
        this.opusFec = opusFec;
        this.opusMusic = opusMusic;
    }

    public int getOpusFrameTime() {
        return opusFrameRate;
    }

    public int getOpusBitrate() {
        return opusBitrate;
    }

    public int getOpusComplexity() {
        return opusComplexity;
    }

    public int getOpusPacketLoss() {
        return opusPacketLoss;
    }

    public boolean isOpusVbr() {
        return opusVbr;
    }

    public boolean isOpusFec() {
        return opusFec;
    }

    public boolean isOpusMusic() {
        return opusMusic;
    }
}