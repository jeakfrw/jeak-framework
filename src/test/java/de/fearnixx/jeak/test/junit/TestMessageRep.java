package de.fearnixx.jeak.test.junit;

import de.fearnixx.jeak.service.locale.MessageRep;
import de.fearnixx.jeak.service.locale.MissingParameterException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestMessageRep {

    private static final String TEST_MESSAGE = "Hello %[world], how are %[you] doing?";
    private static final String TEST_MESSAGE_SHORT = "Hello %[world], how are %[you]";
    private static final String TEST_RESULT = "Hello WORLD, how are PEOPLE doing?";
    private static final String TEST_RESULT_SHORT = "Hello WORLD, how are PEOPLE";

    private static Map<String, String> values = new HashMap<>();
    private static MessageRep message;

    @BeforeClass
    public static void initAndAnalyze() {
        values.put("world", "WORLD");
        values.put("you", "PEOPLE");

        message = new MessageRep(TEST_MESSAGE);
    }

    @Test
    public void testAnalyzeResult() {
        Assert.assertArrayEquals(values.keySet().toArray(), message.getRequiredParams().toArray());
        Assert.assertEquals(values.size() + 1, message.getSplitMessageParts().size());
    }

    @Test
    public void testBuildResult() throws MissingParameterException {
        String result = message.getWithParams(values);
        Assert.assertEquals(TEST_RESULT, result);
    }

    @Test(expected = MissingParameterException.class)
    public void testMissingParams() throws MissingParameterException {
        final String result = message.getWithParams(Collections.singletonMap("world", "missing?"));
    }

    @Test
    public void testRepeatWithoutRight() throws MissingParameterException {
        message = new MessageRep(TEST_MESSAGE_SHORT);
        Assert.assertArrayEquals(values.keySet().toArray(), message.getRequiredParams().toArray());
        Assert.assertEquals(values.size(), message.getSplitMessageParts().size());

        final String result = message.getWithParams(values);
        Assert.assertEquals(TEST_RESULT_SHORT, result);
    }
}
