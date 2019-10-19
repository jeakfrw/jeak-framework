package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.antlr.CommandExecutionCtxParser;
import de.fearnixx.jeak.antlr.CommandExecutionCtxParserBaseVisitor;
import de.mlessmann.confort.lang.ParseVisitException;

public class CommandCtxVisitor extends CommandExecutionCtxParserBaseVisitor<Void> {

    private CommandInfo commInfo = new CommandInfo();

    @Override
    public Void visitCommandExecution(CommandExecutionCtxParser.CommandExecutionContext ctx) {
        if (ctx.argument() != null) {
            ctx.argument().forEach(this::visitArgument);
        } else if (ctx.parameter() != null) {
            ctx.parameter().forEach(this::visitParameter);
        }
        return null;
    }

    @Override
    public Void visitArgument(CommandExecutionCtxParser.ArgumentContext ctx) {
        if (ctx.ARGUMENT_OPTION() != null) {
            String option = ctx.ARGUMENT_OPTION().getText();
            commInfo.getArguments().put(option, "");
        } else if (ctx.ARGUMENT_HEAD_EQ() != null) {
            String nameStr = ctx.ARGUMENT_HEAD_EQ().getText();
            nameStr = nameStr.replaceAll("^--?", "");
            nameStr = nameStr.replaceAll("=$", "");
            String name = nameStr;

            if (ctx.parameter() != null) {
                String value = extractParamStr(ctx.parameter());
                commInfo.getArguments().put(name, value);
            } else {
                commInfo.getArguments().put(name, "");
            }
        }
        return null;
    }

    @Override
    public Void visitParameter(CommandExecutionCtxParser.ParameterContext ctx) {
        String value = extractParamStr(ctx);
        commInfo.getParameters().add(value);
        return null;
    }

    private String extractParamStr(CommandExecutionCtxParser.ParameterContext ctx) {
        String value = "";
        if (ctx.QUOTED_PARAMETER() != null) {
            value = ctx.QUOTED_PARAMETER().getText();

        } else if (ctx.PARAMETER() != null) {
            value = ctx.PARAMETER().getText();

        } else {
            throw new ParseVisitException("Cannot read non quouted/unquoted parameter!");
        }
        //noinspection RegExpRedundantEscape
        value = value.replaceAll("\\\"", "\"");
        return value;
    }

    public CommandInfo getInfo() {
        return commInfo;
    }
}
