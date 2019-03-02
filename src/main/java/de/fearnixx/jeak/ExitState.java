package de.fearnixx.jeak;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExitState {

    private List<ExecutorService> executors = new LinkedList<>();

    private final JeakBot bot;

    public ExitState(JeakBot bot) {
        this.bot = bot;
    }

    public List<ExecutorService> getExecutors() {
        return executors;
    }

    public JeakBot getBot() {
        return bot;
    }
}
