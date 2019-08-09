package de.fearnixx.jeak.service.command;

/**
 * There's no need to send messages when a parameter wasn't accepted.
 * Just throw this exception.
 */
public class CommandParameterException extends CommandException {

    private String paramName;
    private String passedValue;

    /**
     * {@inheritDoc}
     */
    public CommandParameterException(String message) {
        super(message);
    }

    /**
     * Constructs a parameter exception that will tell a little more about the param name, given/parsed value and a message.
     */
    public CommandParameterException(String message, String paramName, String passedValue) {
        super(message);
        this.paramName = paramName;
        this.passedValue = passedValue;
    }

    /**
     * @see #CommandParameterException(String, String, String)
     */
    public CommandParameterException(String message, String paramName, String passedValue, Throwable cause) {
        super(message, cause);
        this.paramName = paramName;
        this.passedValue = passedValue;
    }

    /**
     * The name of the invalid parameter.
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * The given OR parsed value for the invalid parameter.
     */
    public String getPassedValue() {
        return passedValue;
    }
}
