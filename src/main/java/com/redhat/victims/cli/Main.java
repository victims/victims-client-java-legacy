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

import com.redhat.victims.cli.commands.LastUpdateCommand;
import com.redhat.victims.cli.commands.ConfigureCommand;
import com.redhat.victims.cli.commands.DumpCommand;
import com.redhat.victims.cli.commands.CompareCommand;
import com.redhat.victims.cli.commands.ScanFileCommand;
import com.redhat.victims.cli.commands.SynchronizeCommand;
import com.redhat.victims.cli.commands.PomScannerCommand;
import com.redhat.victims.cli.commands.ScanDirCommand;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * A simple wrapper entry point to the REPL.

 * @author gm
 */
public class Main {

    // Flags
    static final String VERBOSE_FLAG            = "--verbose";
    static final String HELP_FLAG               = "--help";
    static final String SYNC_FLAG               = "--update";
    static final String REPL_FLAG               = "--repl";
    static final String STAT_FLAG               = "--db-status";
    static final String RECUR_FLAG              = "--recursive";
    static final String VICTIMS_PURGE_DB        = "--victims-db-purge";
    static final String VICTIMS_PURGE_CACHE     = "--victims-cache-purge";

    final static Map<String, String> FLAGS;

    static {
        Map<String, String> flags = new HashMap();
        flags.put(VERBOSE_FLAG,         "show verbose output");
        flags.put(HELP_FLAG,            "show this help message");
        flags.put(SYNC_FLAG,            "synchronize with the victims web service");
        flags.put(REPL_FLAG,            "run an interactive victims shell");
        flags.put(STAT_FLAG,            "displays date the database was last updated");
        flags.put(RECUR_FLAG,           "recurses directory structure when scanning a directory");
        flags.put(VICTIMS_PURGE_DB,     "purges all entries in the victims database");
        flags.put(VICTIMS_PURGE_CACHE,  "purges all entries in the victims cache");

        FLAGS = Collections.unmodifiableMap(flags);
    };

    // Options with arguments
    static final String JAR_INFO                = "--jar-info";
    static final String COMPARE_JARS            = "--jar-compare";
    static final String VICTIMS_HOME            = "--victims-home";
    static final String VICTIMS_DB_USER         = "--victims-db-user";
    static final String VICTIMS_DB_PASS         = "--victims-db-pass";
    static final String VICTIMS_DB_URL          = "--victims-db-url";
    static final String VICTIMS_DB_DRIVER       = "--vicitms-db-driver";
    static final String VICTIMS_SERVICE_URI     = "--victims-service-uri";
    static final String VICTIMS_SERVICE_ENTRY   = "--victims-service-entry";

    final static Map<String, String> OPTIONS;

    static {
        Map<String, String> tmp = new HashMap();
        tmp.put(JAR_INFO,               "displays fingerprint information of the supplied jar file");
        tmp.put(COMPARE_JARS,           "compare the hashes of two jar files. Specify the jar files with , as a delimiter, i.e. file1.jar,file2.jar");
        tmp.put(VICTIMS_HOME,           "set the directory where victims data should be stored");
        tmp.put(VICTIMS_DB_USER,        "set the user to connect to the victims database");
        tmp.put(VICTIMS_DB_PASS,        "set the password to use to connect to the victims database");
        tmp.put(VICTIMS_DB_URL,         "the jdbc url connection string to use with the victims database");
        tmp.put(VICTIMS_DB_DRIVER,      "the jdbc driver to use when connecting to the victims database");
        tmp.put(VICTIMS_SERVICE_URI,    "the uri to use to synchronize with the victims database");
        tmp.put(VICTIMS_SERVICE_ENTRY,  "the uri path to use when connecting to the victims service");
        OPTIONS = Collections.unmodifiableMap(tmp);
    };


    public static void main(String args[]) {
        new Main().runWithArgs(args);
    }

    private static String extractValue(CommandLineOptions opts, String key){
        CommandLineOption<String> opt = opts.getOption(key);
        if (opt.hasValue()){
            return opt.getValue();
        }
        return null;
    }

    private static void setConfig(Repl repl, CommandLineOptions opts, String cfgitem){
        if (opts.getOption(cfgitem).hasValue()){
            repl.runCommand(ConfigureCommand.COMMAND_NAME, "set", extractValue(opts, cfgitem));
        }
    }

    public void runWithArgs(String args[]){

        // argv[0]?
        String programName = "victims-client";

        StringBuilder description = new StringBuilder();
        description.append("The victims java client will scan any supplied ")
            .append(String.format("%n"))
            .append("pom.xml, jar files or directories supplied as command line ")
            .append(String.format("%n"))
            .append("arguments. By default the output will only report when a ")
            .append(String.format("%n"))
            .append("vulnerable component was detected, or if an error condition exists. ")
            .append(String.format("%n"))
            .append(String.format("%n"))
            .append("EXAMPLES:")
            .append(String.format("%n"))

            // scan a pom
            .append("  Scanning a pom.xml file:")
            .append(String.format("%n    "))
            .append(programName)
            .append("  --verbose project/pom.xml")
            .append(String.format("%n%n"))

            // scan a jar
            .append("  Scanning a jar file:")
            .append(String.format("%n    "))
            .append(programName)
            .append(" --update --verbose file.jar")
            .append(String.format("%n%n"))

            // scan a directory
            .append("  Scanning a directory of files:")
            .append(String.format("%n    "))
            .append(programName)
            .append(" --verbose --update --recursive  project/pom.xml")
            .append(String.format("%n%n"))

            // print fingerprint information
            .append("  Printing a jar file fingerprint:")
            .append(String.format("%n    "))
            .append(programName)
            .append(" --jar-info  file.jar")
            .append(String.format("%n%n"))

            // run an interactive repl
            .append("  Run in interactive mode:")
            .append(String.format("%n    "))
            .append(programName)
            .append(" --repl")
            .append(String.format("%n"));


        CommandLineOptions opts = new CommandLineOptions(programName);
        opts.setDescription(description.toString());

        for (String key : FLAGS.keySet()){
            opts.addOption(new CommandLineOption<Boolean>(key, FLAGS.get(key),
                    false, false, true, null, Boolean.class));
        }

        for (String key : OPTIONS.keySet()){
            opts.addOption(new CommandLineOption<String>(key, OPTIONS.get(key),
                    false, true, false, null, String.class));
        }

        if (args.length == 0 || ! opts.parse(args)){
            System.err.println(opts.getUsage());
            System.exit(2);
        }

        if (opts.getOption(REPL_FLAG).flagSet()){
            System.setProperty(Repl.INTERACTIVE, "true");
        }

        if (opts.getOption(VERBOSE_FLAG).flagSet()){
            System.setProperty(Repl.VERBOSE, "true");
        }

        Repl repl = new Repl();
        repl.register(new ConfigureCommand());
        repl.register(new LastUpdateCommand());
        repl.register(new SynchronizeCommand());
        repl.register(new ScanFileCommand());
        repl.register(new ScanDirCommand(opts.getOption(RECUR_FLAG).flagSet(), repl));
        repl.register(new PomScannerCommand());
        repl.register(new DumpCommand());
        repl.register(new CompareCommand());

        // Handle triggered flags
        if (opts.getOption(HELP_FLAG).flagSet()){
            System.err.println(opts.getUsage());
            System.exit(1);
        }

        // Configure victims environment overrides.
        setConfig(repl, opts, VICTIMS_HOME);
        setConfig(repl, opts, VICTIMS_DB_PASS);
        setConfig(repl, opts, VICTIMS_DB_USER);
        setConfig(repl, opts, VICTIMS_DB_URL);
        setConfig(repl, opts, VICTIMS_DB_DRIVER);
        setConfig(repl, opts, VICTIMS_SERVICE_URI);
        setConfig(repl, opts, VICTIMS_SERVICE_ENTRY);

        if (opts.getOption(STAT_FLAG).flagSet()){
            repl.runCommand(LastUpdateCommand.COMMAND_NAME);
        }

        if (opts.getOption(SYNC_FLAG).flagSet()){
            System.out.println("Updating EVD definitions...");
            repl.runCommand(SynchronizeCommand.COMMAND_NAME);
        }

        if (opts.getOption(REPL_FLAG).flagSet()){
            System.out.println("Entering interactive mode.");
            System.exit(repl.loop());
        }

        if (opts.getOption(JAR_INFO).hasValue()){
            String val = extractValue(opts, JAR_INFO);
            if (val != null){
                repl.runCommand(DumpCommand.COMMAND_NAME, val);
                try {
                    repl.shutdown(true);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }

        if (opts.getOption(COMPARE_JARS).hasValue()){
            String val = extractValue(opts, COMPARE_JARS);
            if (val != null){
                repl.runCommand(CompareCommand.COMMAND_NAME, val);
                try {
                    repl.shutdown(true);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }

        // Remaining files / directories as arguments
        for (String arg : opts.getArguments()) {

            File path = new File(arg);
            if (!path.exists()) {
                System.out.println("path not found - " + path.getName());

            } else {

                String command;
                if (path.isDirectory()) {
                    command = ScanDirCommand.COMMAND_NAME;
                } else if (arg.endsWith(".xml")) {
                    command = PomScannerCommand.COMMAND_NAME;
                } else {
                    command = ScanFileCommand.COMMAND_NAME;
                }
                if (opts.getOption(VERBOSE_FLAG).flagSet())
                    System.out.println("Scanning - " + arg);

                repl.runCommand(command, arg);
            }
        }

        try{
            while (repl.scheuduled() > 0 ){
                repl.processCompleted(-1);

            }
            repl.shutdown(true);


        } catch (InterruptedException e){
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
