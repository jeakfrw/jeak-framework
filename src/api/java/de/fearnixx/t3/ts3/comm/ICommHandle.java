package de.fearnixx.t3.ts3.comm;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Life4YourGames on 05.07.17.
 */
public interface ICommHandle {

    String getPluginID();

    String getID();

    Optional<BiConsumer<ICommChannel, ICommMessage>> getOnMessage();

    class Builder {

        private String id = null;
        private String pluginID = null;
        private BiConsumer<ICommChannel, ICommMessage> onMessage;

        public Builder() {}

        public Builder pluginID(String pluginID) {
            this.pluginID = pluginID;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder onMessage(BiConsumer<ICommChannel, ICommMessage> onMessage) {
            this.onMessage = onMessage;
            return this;
        }

        public Builder reset() {
            this.pluginID = null;
            this.id = null;
            this.onMessage = null;
            return this;
        }

        public ICommHandle build() {
            if (pluginID == null
                    || id == null
                    || onMessage == null)
                throw new IllegalArgumentException("pluginID, id and onMessage may not be null!");
            final String fPluginID = pluginID;
            final String fID = id;
            final BiConsumer<ICommChannel, ICommMessage> fOnMessage = onMessage;
            return new ICommHandle() {
                @Override
                public String getPluginID() {
                    return fPluginID;
                }

                @Override
                public String getID() {
                    return fID;
                }

                @Override
                public Optional<BiConsumer<ICommChannel, ICommMessage>> getOnMessage() {
                    return Optional.of(fOnMessage);
                }
            };
        }
    }
}
