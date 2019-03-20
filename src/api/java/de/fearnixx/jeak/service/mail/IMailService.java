package de.fearnixx.jeak.service.mail;

import java.util.Optional;

public interface IMailService {

    Optional<ITransportUnit> getTransportUnit(String unitName);
}
