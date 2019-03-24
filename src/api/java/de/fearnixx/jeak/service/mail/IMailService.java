package de.fearnixx.jeak.service.mail;

import java.util.Optional;

public interface IMailService {

    /**
     * Optionally, returns the transport unit for the supplied name.
     */
    Optional<ITransportUnit> getTransportUnit(String unitName);
}
