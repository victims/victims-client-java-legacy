package com.redhat.victims.cli;

/*
 * #%L
 * This file is part of victims-client.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.redhat.victims.cli.commands.Command;
import com.redhat.victims.cli.commands.MapCommand;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.commands.HelpCommand;
import com.redhat.victims.cli.commands.ExitCommand;
import com.redhat.victims.cli.results.ExitTerminate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author gm
 */
public class Repl {

    public static final String INTERACTIVE = "victims.cli.repl";
    public static final String VERBOSE = "victims.cli.verbose";

    private String prompt;
    private BufferedReader in;
    private PrintStream out;
    private Map<String, Command> commands;
    private boolean verbose;
    private boolean interactive;

    private ExecutorService executor;
    private ExecutorCompletionService<CommandResult> completor;
    private List<Future<CommandResult>> completed;

    Repl(InputStream input, PrintStream output, String prompt) {
        this.in = new BufferedReader(new InputStreamReader(input));
        this.out = output;
        this.prompt = prompt;
        this.commands = new HashMap();

        String v = System.getProperty(VERBOSE);
        this.verbose = (v != null && v.equals("true"));

        String i = System.getProperty(INTERACTIVE);
        this.interactive = (i != null && i.equals("true"));

        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        completor = new ExecutorCompletionService(executor);
        completed = new ArrayList();

        register(new MapCommand(this.commands, completor, completed));
        register(new HelpCommand(this.commands));
        register(new ExitCommand());
    }

    Repl() {
        this(System.in, System.out, ">");
    }

    final void register(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public void shutdown(boolean wait) throws InterruptedException, ExecutionException {
        try {
            for (Future<CommandResult> task : completed) {
                if (wait) {
                    print(task.get());
                }
                task.cancel(true);
            }
        } finally {

            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    private static String unquote(String s) {
        if (s.isEmpty()) {
            return s;
        }
        int start = 0;
        int end = s.length();
        if (s.startsWith("\'") || s.startsWith("\"")) {
            start++;
        }
        if (s.endsWith("\'") || s.endsWith("\"")) {
            end--;
        }

        return s.substring(start, end);

    }

    private static List<String> parse(String cmd) {

        List<String> tokens = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
        Matcher regexMatcher = regex.matcher(cmd);
        while (regexMatcher.find()) {
            tokens.add(unquote(regexMatcher.group()));
        }
        return tokens;
    }

    final String read() {
        try {
            if (interactive) {
                out.printf("%s ", prompt);
            }
            return in.readLine();

        } catch (IOException e) {
        }

        return null;
    }

    final Command eval(String cmd) {
        if (cmd == null) {
            return null;
        }

        List<String> tokens = parse(cmd);

        if (tokens.isEmpty()) {
            return null;
        }

        String commandName = tokens.remove(0);
        Command c = commands.get(commandName);

        if (c == null) {
            out.printf("invalid command: %s%n", commandName);
            return null;
        }
        c = c.newInstance();

        if (tokens.isEmpty()) {
            c.setArguments(null);
        } else {
            c.setArguments(tokens);
        }

        return c;

    }

    final void print(CommandResult r) {

        if (r == null || r.getOutput() == null || r.getOutput().isEmpty()) {
            return;
        }

        if (verbose && r.getVerboseOutput() != null) {
            out.println(r.getVerboseOutput());

        } else {
            out.println(r.getOutput());
        }

    }

    final int loop() {

        int rc = 0;

        try {

            while (true) {

                String input = null;
                boolean didRead = false;

                if (in.ready() || completed.isEmpty()) {
                    input = read();
                    didRead = true;
                }

                if (input == null && didRead) {
                    shutdown(true);

                    if (interactive) {
                        out.println();
                    }

                    break; // eof
                }

                // submit async job
                if (input != null) {
                    Command callable = eval(input);
                    if (callable != null) {
                        completed.add(completor.submit(callable));
                    }
                }

                // process completed jobs 
                Future<CommandResult> result;
                while ((result = completor.poll()) != null) {
                    CommandResult r = result.get();
                    completed.remove(result);

                    if (r != null) {

                        print(r);

                        // bail
                        if (r.failed() || r instanceof ExitTerminate) {
                            shutdown(false);
                            return r.getResultCode();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.printf("error: %s%n", e.getMessage());
            rc = CommandResult.RESULT_ERROR;
        } catch (InterruptedException e) {
            e.printStackTrace();

            out.printf("error: %s%n", e.getMessage());
            rc = CommandResult.RESULT_ERROR;
        } catch (ExecutionException e) {
            e.printStackTrace();

            out.printf("error: %s%n", e.getMessage());
            rc = CommandResult.RESULT_ERROR;

        }

        return rc;
    }

}
