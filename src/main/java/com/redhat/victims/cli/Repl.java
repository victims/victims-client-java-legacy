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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redhat.victims.cli.commands.Command;
import com.redhat.victims.cli.commands.ExitCommand;
import com.redhat.victims.cli.commands.HelpCommand;
import com.redhat.victims.cli.commands.MapCommand;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitTerminate;


/**
 * This started out as a basic REPL implementation with the
 * goal of dispatching different commands related to victims
 * usage. The initial implementation has been perverted a
 * little to allow for asynchronous dispatch of commands
 * with the aim of facilitating better performance (at the cost
 * of this mess).
 *
 * It uses an executor to dispatch invocations of commands
 * and watches for task completions.
 *
 * @author gm
 */
public class Repl {

    /** System property to run in interactive mode */
    public static final String INTERACTIVE = "victims.cli.repl";

    /** System property to run in verbose mode */
    public static final String VERBOSE = "victims.cli.verbose";

    /** Regex-fu to parse input into the REPL */
    private static Pattern commandPattern = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");

    /** Prompt to display in interactive mode */
    private final String prompt;

    /** Source of input to the REPL */
    private final BufferedReader in;

    /** Output source for the REPL */
    private final PrintStream out;

    /** Symbol table for commands */
    private final Map<String, Command> commands;

    /** Are we in verbose mode? */
    private final boolean verbose;

    /** Are we in interactive mode */
    private final boolean interactive;

    /** Are we currently being shutdown (don't schedule any more commands) */
    private boolean shuttingDown;

    /** Executor service used to asynchronously perform commands */
    private final ExecutorService executor;

    /** The completer service used with the executor */
    private final ExecutorCompletionService<CommandResult> completor;

    /** The number of jobs currently scheduled to be run. */
    private final AtomicInteger running;


    /**
     * Instantiate a new REPL using the supplied input and output
     * sources to interact with the user. The default commands
     * of help, map, and exit are registered by default.
     *
     * @param input The input source to read commands from
     * @param output The output source to display results of the commands
     * @param commandPrompt The prompt to show the user in interactive mode
     */
    Repl(InputStream input, PrintStream output, String commandPrompt) {

        in = new BufferedReader(new InputStreamReader(input));
        out = output;
        prompt = commandPrompt;
        commands = new HashMap<String, Command>();

        String v = System.getProperty(VERBOSE);
        verbose = (v != null && v.equals("true"));

        String i = System.getProperty(INTERACTIVE);
        interactive = (i != null && i.equals("true"));

        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        completor = new ExecutorCompletionService<CommandResult>(executor);
        running = new AtomicInteger(0);

        // built-in commands
        register(new MapCommand(this));
        register(new HelpCommand(this.commands));
        register(new ExitCommand());

        shuttingDown = false;
    }

    /**
     * Default constructor. Uses stdin and stdout for i/o and '>' for the prompt.
     */
    Repl() {
        this(System.in, System.out, ">");
    }

    /**
     * Used to register a command with the REPL.
     * @param cmd Command to be registered.
     */
    final void register(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    /**
     * Used to poll the completer service for the next completed job.
     *
     * @param timeout A negative value means 'wait forever', a positive
     * value will be the time to wait in milliseconds.
     * @return Future containing the command results, or null if an
     * error occurred.
     */
    public Future<CommandResult> poll(int timeout){

        if (timeout < 0){
            return completor.poll();
        } else{
            try{
                return completor.poll(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e){
                if (verbose){
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

    /**
     * Schedule the execution of the supplied command. This is used to
     * track number of running tasks, as well as to prevent submission
     * when the REPL is attempting to shutdown.
     * @param cmd
     */
    private void scheduleExecution(Command cmd){
        if (! shuttingDown){
            running.incrementAndGet();
            completor.submit(cmd);
        }
    }

    /**
     * Scheduled the supplied command for asynchronous execution by the
     * execution service. This is a convenience method more than anything.
     *
     * @param commandName
     * @param optionalArguments
     */
    public void runCommand(String commandName, String... optionalArguments){

        Command cmd = getCommand(commandName);
        if (cmd != null){
            ArrayList<String> args = new ArrayList<String>();
            args.addAll(Arrays.asList(optionalArguments));
            cmd.setArguments(args);
            scheduleExecution(cmd);
        }
    }

    /**
     * Run a command and wait for its completion. This provides a way to execute
     * commands in series.
     *
     * @param commandName
     * @param optionalArguments
     */
    public void runSynchronousCommand(String commandName, String... optionalArguments){
        Command cmd = getCommand(commandName);
        if (cmd != null){
            ArrayList<String> args = new ArrayList<String>();
            args.addAll(Arrays.asList(optionalArguments));
            cmd.setArguments(args);
            CommandResult r = cmd.execute(args);
            if (r != null){
                print(r);
                if (r.failed() || r instanceof ExitTerminate){
                    shutdown();
                }
                if (r instanceof ExitTerminate){
                    System.exit(r.getResultCode());
                }
            }
        }
    }

    /**
     * Returns the number of tasks that are currently running.
     * @return
     */
    public int scheduled(){
        return running.get();
    }

    /**
     * Returns an new instance of a command via the command name.
     * @param key The name of the command
     * @return A Command instance or null if the command is invalid.
     */
    public Command getCommand(String key){
        Command c = commands.get(key);
        if (c != null){
            return c.newInstance();
        }
        return null;
    }

    /**
     * Returns an iterator that can be used to process commands that
     * have completed.
     *
     * @return
     */
    public Iterable<CommandResult> completedCommands(){
        return new CommandResults();
    }

    /**
     * Instruct the executor service to shutdown. Prevents any future
     * commands being issued.
     */
    public void shutdown() {
        shuttingDown = true;
        if (executor != null){
            executor.shutdown();
        }
    }

    /**
     * Used to remove enclosing quotes from a string.
     * @param s The string to unquote
     * @return The string provided without leading and trailing quote marks.
     */
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


    /**
     * Convert the supplied input string into a series of tokens.
     * @param cmd Command string.
     * @return Tokenized command string.
     */
    private static List<String> parse(String cmd) {

        List<String> tokens = new ArrayList<String>();
        Matcher regexMatcher = commandPattern.matcher(cmd);
        while (regexMatcher.find()) {
            tokens.add(unquote(regexMatcher.group()));
        }
        return tokens;
    }

    /**
     * The 'R' in REPL.
     * @return
     */
    public final String read() {
        try {
            if (interactive) {
                out.printf("%s ", prompt);
            }
            return in.readLine();

        } catch (IOException e) {
            if (verbose){
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * The 'E' in REPL.
     * @param cmd
     * @return
     */
    public final Command eval(String cmd) {
        if (cmd == null) {
            return null;
        }

        List<String> tokens = parse(cmd);

        if (tokens.isEmpty()) {
            return null;
        }

        String commandName = tokens.remove(0);
        Command c = getCommand(commandName);
        if (c == null) {
            out.printf("invalid command: %s%n", commandName);
            return null;
        }

        if (tokens.isEmpty()) {
            c.setArguments(null);
        } else {
            c.setArguments(tokens);
        }

        return c;

    }

    /**
     * The 'P' in REPL.
     * @param r
     */
    public final void print(CommandResult r) {

        if (r == null || r.getOutput() == null || r.getOutput().isEmpty()) {
            return;
        }

        out.println(r.getOutput());

        if (verbose && r.getVerboseOutput() != null) {
            out.println(r.getVerboseOutput());
        }

    }

    /**
     * The 'L' in REPL.
     * @return
     */
    public final int loop() {



        try {

            while (true) {

                String input = null;
                boolean didRead = false;
                if (in.ready() || scheduled() == 0) {
                    input = read();
                    didRead = true;
                }

                if (input == null && didRead) {
                    processOutstanding();
                    shutdown();

                    if (interactive) {
                        out.println();
                    }

                    break; // eof
                }

                // submit async job
                if (input != null && ! shuttingDown ) {
                    Command callable = eval(input);
                    if (callable != null) {
                        scheduleExecution(callable);

                    }
                }

                // handle completed jobs
                CommandResult error = processOutstanding();
                if (error != null){
                    return error.getResultCode();
                }
            }

        } catch (IOException e) {
            out.printf("ERROR: %s%n", e.getMessage());
            if (verbose){
                e.printStackTrace();
            }
            return CommandResult.RESULT_ERROR;
        }

        return 0;
    }

    /**
     * Iterate through outstanding commands.
     * @return The rc for completed commands (non zero in error).
     */
    private CommandResult processOutstanding(){
        for (CommandResult result : completedCommands()){
            print(result);
            if (result != null && (result.failed() || result instanceof ExitTerminate)){
                return result;
            }
        }
        return null;
    }

    /**
     * Provide iterator interface for consumers to process completed commands
     */
    class CommandResults implements Iterable<CommandResult>, Iterator<CommandResult> {
        @Override
        public boolean hasNext() {
            return scheduled() > 0;
        }

        @Override
        public CommandResult next() {

            Future<CommandResult> future = poll(-1);
            if (future == null){
                return null;
            }

            try {
                CommandResult cmd = future.get();
                running.decrementAndGet();
                return cmd;

            } catch (InterruptedException e){
                Throwable cause = e.getCause();
                if (cause != null){
                    CommandResult interruptedError = new ExitTerminate(CommandResult.RESULT_ERROR);
                    interruptedError.addOutput("ERROR: Command was interrupted and unable to complete");
                    interruptedError.addOutput(cause.toString());
                    return interruptedError;
                }

            } catch (ExecutionException e){
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof RuntimeException ){
                    CommandResult runtimeError = new ExitTerminate(CommandResult.RESULT_ERROR);
                    runtimeError.addOutput("ERROR: Unexpected runtime error occurred!\n");
                    runtimeError.addOutput(cause.toString());
                    return runtimeError;
                }
            }
            return null;
        }

        @Override
        public void remove() {

        }
        @Override
        public Iterator<CommandResult> iterator() {
            return this;
        }

    }
}
