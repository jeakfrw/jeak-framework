package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.antlr.CommandExecutionCtxParser;
import de.fearnixx.jeak.antlr.CommandExecutionCtxParserBaseVisitor;
import de.mlessmann.confort.lang.RuntimeParseException;

import java.util.Arrays;

public class CommandCtxVisitor extends CommandExecutionCtxParserBaseVisitor<Void> {

    private CommandInfo commInfo = new CommandInfo();

    @Override
    public Void visitCommandExecution(CommandExecutionCtxParser.CommandExecutionContext ctx) {
        if (ctx.argument() != null && !ctx.argument().isEmpty()) {
            ctx.argument().forEach(this::visitArgument);
        } else if (ctx.parameter() != null && !ctx.parameter().isEmpty()) {
            ctx.parameter().forEach(this::visitParameter);
        }
        return null;
    }

    @Override
    public Void visitArgument(CommandExecutionCtxParser.ArgumentContext ctx) {
        if (ctx.ARGUMENT_OPTION() != null) {
            String nameStr = ctx.ARGUMENT_OPTION().getText();
            boolean multiflag = false;
            if (nameStr.startsWith("--")) {
                nameStr = nameStr.substring(2);

            } else if (nameStr.startsWith("-")) {
                nameStr = nameStr.substring(1);
                multiflag = true;
            }
            nameStr = cropEquals(nameStr);

            if (multiflag) {
                addMultiflagOption(nameStr);
            } else {
                commInfo.getArguments().put(nameStr, "true");
            }
        } else if (ctx.ARGUMENT_HEAD_EQ() != null) {
            String nameStr = ctx.ARGUMENT_HEAD_EQ().getText();
            boolean multiflag = false;
            if (nameStr.startsWith("--")) {
                nameStr = nameStr.substring(2);

            } else if (nameStr.startsWith("-")) {
                nameStr = nameStr.substring(1);
                multiflag = true;
            }
            nameStr = cropEquals(nameStr);

            if (!multiflag) {
                String value = null;
                if (ctx.parameter() != null) {
                    value = extractParamStr(ctx.parameter());
                }
                commInfo.getArguments().put(nameStr, value != null ? value : "");
            } else {
                addMultiflagOption(nameStr);
            }
        }
        return null;
    }

    private String cropEquals(String nameStr) {
        if (nameStr.endsWith("=")) {
            nameStr = nameStr.substring(0, nameStr.length() - 1);
        }
        return nameStr;
    }

    private void addMultiflagOption(String nameStr) {
        String[] flags = nameStr.split(".");
        Arrays.stream(flags).forEach(shortHand ->
                commInfo.getArguments().put(shortHand, "true"));
    }

    @Override
    public Void visitParameter(CommandExecutionCtxParser.ParameterContext ctx) {
        String value = extractParamStr(ctx);
        if (value == null) {
            throw new RuntimeParseException(
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    ctx.getStart().getTokenSource().getSourceName(),
                    "Cannot read non quouted/unquoted parameter!"
            );
        }
        commInfo.getParameters().add(value);
        return null;
    }

    private String extractParamStr(CommandExecutionCtxParser.ParameterContext ctx) {
        String value = "";
        if (ctx.QUOTED_PARAMETER() != null) {
            value = ctx.QUOTED_PARAMETER()
                    .getSymbol()
                    .getText();
            // Cut off the quotes;
            value = value.substring(1, value.length() - 1);

        } else if (ctx.PARAMETER() != null) {
            value = ctx.PARAMETER().getText();

        } else {
            return null;
        }
        //noinspection RegExpRedundantEscape
        value = value.replaceAll("\\\\(.)", "$1");
        return value;
    }

    public CommandInfo getInfo() {
        return commInfo;
    }
}
