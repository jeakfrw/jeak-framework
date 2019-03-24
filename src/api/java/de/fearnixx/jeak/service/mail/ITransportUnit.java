package de.fearnixx.jeak.service.mail;

/**
 * Abstract representation of a configured SMTP connection.
 */
public interface ITransportUnit {

    /**
     * Schedules a message for transmission.
     * @implNote Transmission is performed asynchronously
     */
    void dispatch(IMail message);
}
