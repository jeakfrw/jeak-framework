package de.fearnixx.jeak.test.junit.antlr;

import de.fearnixx.jeak.service.command.CommandInfo;
import de.fearnixx.jeak.service.command.TypedCommandService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestCommandParser {

    private static final Logger logger = LoggerFactory.getLogger(TestCommandParser.class);

    private StubCommandSvc svcStub = new StubCommandSvc();

    @Test
    public void testNormalParameters() {
        String params = "these are four params";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("these", "are", "four", "params"));
    }

    @Test
    public void testQuotedParameters() {
        String params = "there are \"quoted params\"";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("there", "are", "quoted params"));
    }

    @Test
    public void testQuotedEscapedParameters() {
        String params = "there are \"escapes of \\\" in params\"";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("there", "are", "escapes of \" in params"));
    }

    @Test
    public void testOptionArguments() {
        String params = "--option --option2";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("true"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("true"));
    }

    @Test
    public void testValueArguments() {
        String params = "--option=value --option2=value2";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("value"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("value2"));
    }

    @Test
    public void testQuotedValueArguments() {
        String params = "--option=\"quoted value\" --option2=value";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("quoted value"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("value"));
    }

    @Test
    public void testMixedArgumentParams() {
        String params = "this --shouldnt --work=now";
        CommandInfo info = svcStub.parseLine(params);
        assertThat(info.getErrorMessages().size(), greaterThan(0));
    }

    private static class StubCommandSvc extends TypedCommandService {

        CommandInfo parseLine(String line) {
            return parseCommandLine(line);
        }
    }
}
