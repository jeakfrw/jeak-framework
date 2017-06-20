package de.fearnixx.t3.ts3.channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MarkL4YG on 15.06.17.
 */
public class TS3Spacer extends TS3Channel implements ISpacer {

    public static final Pattern spacerPattern = Pattern.compile("^\\[\\*?c?spacer\\d*\\.*\\d*\\].+$");
    public static final Pattern stripNamePattern = Pattern.compile("^\\[\\*?c?spacer\\d*\\.*\\d*\\](.+)$");
    public static final Pattern stripFloatPattern = Pattern.compile("^\\[\\*?c?spacer(\\d*\\.*\\d*)\\].+$");
    public static final Pattern centeredPattern = Pattern.compile("^\\[\\*?cspacer\\d*\\.*\\d*\\].+$");
    public static final Pattern repeatedPattern = Pattern.compile("^\\[\\*c?spacer(\\d*\\.*\\d*)\\].+$");

    public TS3Spacer() {
        super();
    }

    @Override
    public String getStrippedName() {
        String s = getName();
        Matcher m = stripNamePattern.matcher(s);
        if (m.matches() && m.groupCount() == 1) {
            return m.group(1);
        }
        return s;
    }

    @Override
    public float getNumber() {
        float f = 0f;
        Matcher m = stripFloatPattern.matcher(getName());
        if (m.matches() && m.groupCount() == 1) {
            f = Float.parseFloat(m.group(1));
        }
        return f;
    }

    @Override
    public boolean isCentered() {
        return centeredPattern.matcher(getName()).matches();
    }

    @Override
    public boolean isRepeated() {
        return repeatedPattern.matcher(getName()).matches();
    }
}
