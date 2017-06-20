package de.fearnixx.t3.test.plugin1;

import de.fearnixx.t3.event.query.IQueryEvent;
import de.fearnixx.t3.event.server.ITS3ServerEvent;
import de.fearnixx.t3.event.state.IBotStateEvent;
import de.fearnixx.t3.IT3Bot;
import de.fearnixx.t3.query.IQueryMessageObject;
import de.fearnixx.t3.query.IQueryRequest;
import de.fearnixx.t3.reflect.annotation.Inject;
import de.fearnixx.t3.reflect.annotation.Listener;
import de.fearnixx.t3.reflect.annotation.T3BotPlugin;
import de.mlessmann.config.api.ConfigLoader;
import de.mlessmann.logging.ILogReceiver;

import java.util.List;
import java.util.Optional;

/**
 * Created by MarkL4YG on 12.06.17.
 */
@T3BotPlugin(id = "testplugin.one", version = "1.0.0", requireAfter = {}, depends = {})
public class TestPluginOne {

    @Inject
    public ILogReceiver log;

    @Inject
    public ConfigLoader loader;

    @Inject
    public IT3Bot myBot;

    public TestPluginOne() {
    }

    @Listener
    public void onLoaded(IBotStateEvent.IPluginsLoaded event) {
        if (log == null) throw new RuntimeException("Log hasn't been injected!");
        log.info("Plugins loaded");
        log.info("My config is at: ", loader.getFile().toURI().toString());
    }

    @Listener
    public void onPreConnect(IBotStateEvent.IPreConnect event) {
        log.fine("PreConnect");
    }

    @Listener
    public void onPostConnect(IBotStateEvent.IPostConnect event) {
        log.fine("PostConnect");
    }

    @Listener
    public void onClientList(ITS3ServerEvent.IDataEvent.IClientsUpdated event) {
        List<IQueryMessageObject> list = event.getServer().getClients();
        int c = list.size();
        for (int i = 0; i < c; i++) {
            Optional<String> nick = list.get(i).getProperty("client_nickname");
            Optional<String> clid = list.get(i).getProperty("clid");
            if (clid.isPresent() && nick.isPresent() && (nick.get().equals("MarkL4YG")) && false) {
                IQueryRequest r = IQueryRequest.builder()
                        .command("sendtextmessage")
                        .addKey("targetmode", "1")
                        .addKey("target", clid.get())
                        .addKey("msg", "Hey there! You're client no." + clid.get())
                        .build();
                myBot.getServer().getConnection().sendRequest(r, this::onAnswer);
            } else {
                log.finest("Wrong client nickname:", nick.orElse("NULL"));
            }
        }
    }

    public void onAnswer(IQueryEvent.IMessage event) {
        if (event.getMessage().getError().getID() == 0) {
            log.fine("Text successful");
        } else {
            log.warning("Poke unsuccessful: ID=", event.getMessage().getError().getID(), "MSG=", event.getMessage().getError().getMessage());
        }
    }

    @Listener
    public void onPreShutdown(IBotStateEvent.IPreShutdown event) {
        log.fine("PreShutdown");
    }

    @Listener
    public void onPostShutdown(IBotStateEvent.IPostShutdown event) {
        log.fine("PostShutdown");
    }
}
