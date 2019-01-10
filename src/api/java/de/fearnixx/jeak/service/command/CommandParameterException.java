package de.fearnixx.jeak.service.command;

/**
 * There's no need to send messages when a parameter wasn't accepted.
 * Just throw this exception.
 *
 * @author MarkL4YG
 */
public class CommandParameterException extends CommandException {

    private String paramName;
    private String passedValue;

    public CommandParameterException(String message) {
        super(message);
    }

    public CommandParameterException(String message, String paramName, String passedValue) {
        super(message);
        this.paramName = paramName;
        this.passedValue = passedValue;
    }

    public CommandParameterException(String message, String paramName, String passedValue, Throwable cause) {
        super(message, cause);
        this.paramName = paramName;
        this.passedValue = passedValue;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getPassedValue() {
        return passedValue;
    }

    public void setPassedValue(String passedValue) {
        this.passedValue = passedValue;
    }
}
