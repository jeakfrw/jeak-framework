package de.fearnixx.t3.ts3.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public interface IQueryRequest {

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String command;
        private Map<String, String> currentObj;
        private List<Map<String, String>> chain;
        private List<String> options;

        public Builder() {
            reset();
        }

        public Builder reset() {
            command = "";
            currentObj =  null;
            chain = new ArrayList<>();
            options = new ArrayList<>();
            newChain();
            return this;
        }

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        public Builder newChain() {
            chain.add(new HashMap<>());
            currentObj = chain.get(chain.size()-1);
            return this;
        }

        public Builder addKey(String key, String value) {
            if (currentObj.containsKey(key))
                currentObj.replace(key, value);
            else
                currentObj.put(key, value);
            return this;
        }

        public Builder addOption(String option) {
            options.add(option);
            return this;
        }

        public IQueryRequest build() {
            return new IQueryRequest() {
                final String fComm = command;
                final List<Map<String, String>> fChain = chain;
                final List<String> fOptions = options;

                @Override
                public String getCommand() {
                    return fComm;
                }

                @Override
                public List<Map<String, String>> getChain() {
                    return fChain;
                }

                @Override
                public List<String> getOptions() {
                    return fOptions;
                }
            };
        }
    }

    String getCommand();

    List<Map<String, String>> getChain();

    List<String> getOptions();
}
