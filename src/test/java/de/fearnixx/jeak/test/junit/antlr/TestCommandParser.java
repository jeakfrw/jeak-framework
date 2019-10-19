package de.fearnixx.jeak.test.junit.antlr;

import de.fearnixx.jeak.antlr.CommandExecutionCtxLexer;
import de.fearnixx.jeak.antlr.CommandExecutionCtxParser;
import de.fearnixx.jeak.service.command.CommandCtxVisitor;
import de.fearnixx.jeak.service.command.CommandInfo;
import de.mlessmann.confort.lang.ParseVisitException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TestCommandParser {

    private static final Logger logger = LoggerFactory.getLogger(TestCommandParser.class);

    private CommandInfo runParse(String command) {
        CodePointCharStream charStream = CharStreams.fromString(command);
        var lexer = new CommandExecutionCtxLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new CommandExecutionCtxParser(tokenStream);

        // Use 2-stage parsing for expression performance
        // https://github.com/antlr/antlr4/blob/master/doc/faq/general.md#why-is-my-expression-parser-slow
        try {
            logger.debug("Trying to run STAGE 1 parsing. (SSL prediction)");
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            var grammarContext = parser.commandExecution();
            var treeVisitor = new CommandCtxVisitor();
            treeVisitor.visitCommandExecution(grammarContext);
            return treeVisitor.getInfo();
        } catch (Exception ex) {
            // STAGE 2
            logger.debug("Trying to run STAGE 2 parsing. (LL prediction)", ex);
            tokenStream.seek(0);
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);

            try {
                var grammarContext = parser.commandExecution();
                var treeVisitor = new CommandCtxVisitor();
                treeVisitor.visitCommandExecution(grammarContext);
                return treeVisitor.getInfo();
            } catch (ParseVisitException e) {
                throw new RuntimeException("NOT IMPLEMENTED!");
            }
        }
    }

    @Test
    public void testNormalParameters() {
        String params = "these are four params";
        CommandInfo info = runParse(params);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("these", "are", "four", "params"));
    }

    @Test
    public void testQuotedParameters() {
        String params = "there are \"quoted params\"";
        CommandInfo info = runParse(params);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("there", "are", "quoted params"));
    }

    @Test
    public void testQuotedEscapedParameters() {
        String params = "there are \"escapes of \\\" in params\"";
        CommandInfo info = runParse(params);
        assertThat(info.isParameterized(), is(true));
        assertThat(info.isArgumentized(), is(false));
        assertThat(info.getParameters(), contains("there", "are", "escapes of \" in params"));
    }

    @Test
    public void testOptionArguments() {
        String params = "--option --option2";
        CommandInfo info = runParse(params);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is(""));
        assertThat(info.getArguments().getOrDefault("option2", null), is(""));
    }

    @Test
    public void testValueArguments() {
        String params = "--option=value --option2=value2";
        CommandInfo info = runParse(params);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("value"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("value2"));
    }

    @Test
    public void testQuotedValueArguments() {
        String params = "--option=\"quoted value\" --option2=value";
        CommandInfo info = runParse(params);
        assertThat(info.isParameterized(), is(false));
        assertThat(info.isArgumentized(), is(true));
        assertThat(info.getArguments().getOrDefault("option", null), is("quoted value"));
        assertThat(info.getArguments().getOrDefault("option2", null), is("value"));
    }

    @Test(expected = Exception.class)
    public void testMixedArgumentParams() {
        String params = "this --shouldnt --work=now";
        CommandInfo info = runParse(params);
        assertThat(info, is(IsNull.nullValue()));
    }
}
