package de.fearnixx.t3.teamspeak.cache;

import de.fearnixx.t3.teamspeak.data.IChannel;
import de.fearnixx.t3.teamspeak.data.IClient;

import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 03-Feb-18
 */
public interface IDataCache {

    Map<Integer, IClient> getClientMap();

    Map<Integer, IChannel> getChannelMap();

    List<IClient> getClients();

    List<IChannel> getChannels();
}
