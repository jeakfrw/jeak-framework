package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.service.command.CommandV2Context;

public interface MetaMatcher {

    int tryMatch(CommandV2Context ctx);
}
