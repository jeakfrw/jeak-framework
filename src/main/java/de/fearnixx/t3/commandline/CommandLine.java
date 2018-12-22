package de.fearnixx.t3.commandline;

import de.fearnixx.t3.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Created by MarkL4YG on 18.06.17.
 */
public class CommandLine implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CommandLine.class);

    private final Object lock = new Object();
    private boolean terminated = false;

    private InputStream in;
    private OutputStreamWriter out;

    public CommandLine(InputStream in, OutputStream out) {
        this.in = in;
        this.out = new OutputStreamWriter(out);
    }

    public void run() {
        StringBuilder b = new StringBuilder(128);
        byte[] buffer = new byte[1024];
        byte[] cc = new byte[1];
        int buffPos = 0;
        boolean lf, cr;

        terminated = false;
        outer: while (true) {
            try {
                do {
                    synchronized (lock) {
                        if (terminated) {
                            logger.info("Commandline closed");
                            break outer;
                        }
                    }
                    Thread.sleep(100);
                } while (in.available() <= 0 && !terminated);
                if (in.read(cc) == -1) {
                    logger.error("Commandline reached EOS");
                    synchronized (lock) {
                        kill();
                        break outer;
                    }
                }


                lf = cc[0] == '\n';
                cr = cc[0] == '\r';
                if (cr) continue; // Ignore carriage-return

                buffer[buffPos++] = cc[0];
                if (lf || buffPos == buffer.length) {
                    b.append(new String(buffer, 0, buffPos-1, Charset.defaultCharset()));
                    buffPos = 0;
                    if (lf) {
                        processCommand(b.toString());
                        b = new StringBuilder(128);
                    }
                }
                Thread.sleep(20);
            } catch (InterruptedException e) {
                continue;
            } catch (IOException e) {
                logger.error("Commandline crashed", e);
                kill();
            }
        }
    }

    private static final Pattern commp = Pattern.compile("[\\w\\d]+");
    private void processCommand(String command) throws IOException {
        if (!commp.matcher(command).matches()) {
            logger.warn("Command not matching: {}", command);
            return;
        }
        switch (command) {
            case "stop":
            case "quit":
            case "exit":
            case "shutdown":
                Main.getInstance().shutdown();
                break;
            case "help":
                logger.info("\nCommands: \n\thelp - Display this page" +
                        "\n\tstop - Shutdown all bots");
                break;
            default:
                logger.warn("Unknown command: {}\n", command);
                break;
        }
    }

    public void kill() {
        synchronized (lock) {
            terminated = true;
            try {
                in.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
