package de.fearnixx.jeak.test.junit.antlr;

import de.fearnixx.jeak.service.command.CommandInfo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.fearnixx.jeak.antlr.CommandParserUtil.parseCommandLine;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestCommandParser {

    private static final Logger logger = LoggerFactory.getLogger(TestCommandParser.class);

    @Test
    public void testNormalParameters() {
        String params = "these are four params";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("these", "are", "four", "params"));
    }

    @Test
    public void testQuotedParameters() {
        String params = "there are \"quoted params\"";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("there", "are", "quoted params"));
    }

    @Test
    public void testNonQuotedDashedParameters() {
        String params = "there-is a non-quoted param";
        CommandInfo commandInfo = parseCommandLine(params, logger);
        assertThat(commandInfo.isParameterized(), is(true));
        assertThat(commandInfo.isArgumentized(), is(false));
        assertThat(commandInfo.getParameters(), contains("there-is", "a", "non-quoted", "param"));
    }

    @Test
    public void testQuotedEscapedParameters() {
        String params = "there are \"escapes of \\\" in params\"";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("there", "are", "escapes of \" in params"));
    }

    @Test
    public void testOptionArguments() {
        String params = "--option --option2";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("true"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("true"));
    }

    @Test
    public void testValueArguments() {
        String params = "--option=value --option2=value2";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("value"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("value2"));
    }

    @Test
    public void testQuotedValueArguments() {
        String params = "--option=\"quoted value\" --option2=value";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("quoted value"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("value"));
    }

    @Test
    public void testMixedArgumentParams() {
        String params = "this --shouldnt --work=now";
        CommandInfo info = parseCommandLine(params, logger);
        assertThat(info.getErrorMessages().size(), greaterThan(0));
    }

    @Test
    public void testNonQuotedDashedArguments() {
        String params = "--this=there-is --a=true --b=\"non-quoted\" --param";
        CommandInfo commandInfo = parseCommandLine(params, logger);
        assertThat(commandInfo.isParameterized(), is(false));
        assertThat(commandInfo.isArgumentized(), is(true));
        assertThat(commandInfo.getArguments().get("this"), is("there-is"));
        assertThat(commandInfo.getArguments().get("a"), is("true"));
        assertThat(commandInfo.getArguments().get("b"), is("non-quoted"));
        assertThat(commandInfo.getArguments().get("param"), is("true"));
    }
}
