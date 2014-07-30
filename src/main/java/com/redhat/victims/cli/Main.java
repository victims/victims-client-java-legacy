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

import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsResultCache;
import com.redhat.victims.cli.commands.*;
import com.redhat.victims.cli.common.VictimsConfigurationHelper;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitTerminate;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * A wrapper entry point to the REPL.

 * @author gm
 */
public class Main {

    // Flags
    static final String VERSION_FLAG            = "--version";
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
        Map<String, String> flags = new HashMap<String, String>();
        flags.put(VERSION_FLAG,         "show client version");
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

    final static Map<String, String> OPTIONS;

    static {
        Map<String, String> tmp = new HashMap();
        tmp.put(JAR_INFO,               "displays fingerprint information of the supplied jar file");
        tmp.put(COMPARE_JARS,           "compare the hashes of two jar files. Specify the jar files with , as a delimiter, i.e. file1.jar,file2.jar");
        for (String key : VictimsConfigurationHelper.getKeys()) {
            tmp.put(VictimsConfigurationHelper.getOptionFromKey(key),
                    VictimsConfigurationHelper.getDescriptionFromKey(key));
        }
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
            repl.runCommand(ConfigureCommand.COMMAND_NAME, "set",
                    VictimsConfigurationHelper.getKeyFromOption(cfgitem),
                    extractValue(opts, cfgitem));
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
                    false, 0, true, null, Boolean.class));
        }

        for (String key : OPTIONS.keySet()){
            
            int nargs = 1;
            if (key.equals(COMPARE_JARS)){
                nargs = 2;
            }
            opts.addOption(new CommandLineOption<String>(key, OPTIONS.get(key),
                    false, nargs, false, null, String.class));
            
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
        repl.register(new VersionCommand());

        // Handle triggered flags
        if (opts.getOption(HELP_FLAG).flagSet()){
            System.err.println(opts.getUsage());
            System.exit(1);
        }

        // Configure victims environment overrides.
        for (String opt : VictimsConfigurationHelper.getOptions()) {
            setConfig(repl, opts, opt);
        }

        if (opts.getOption(VERSION_FLAG).flagSet()){
            repl.runSynchronousCommand(VersionCommand.COMMAND_NAME);
            repl.shutdown();
            System.exit(0);
        }

        if (opts.getOption(STAT_FLAG).flagSet()){
            repl.runSynchronousCommand(LastUpdateCommand.COMMAND_NAME);
        }

        if (opts.getOption(SYNC_FLAG).flagSet()){
            System.out.println("Updating EVD definitions...");
            repl.runSynchronousCommand(SynchronizeCommand.COMMAND_NAME);
        }

        if (opts.getOption(REPL_FLAG).flagSet()){
            System.out.println("Entering interactive mode.");
            System.exit(repl.loop());
        }

        if (opts.getOption(JAR_INFO).hasValue()){
            String val = extractValue(opts, JAR_INFO);
            if (val != null){
                repl.runSynchronousCommand(DumpCommand.COMMAND_NAME, val);
                repl.shutdown();
            }
            System.exit(0);
        }

        if (opts.getOption(COMPARE_JARS).hasValue()){
            CommandLineOption<String> opt = opts.getOption(COMPARE_JARS);
            repl.runSynchronousCommand(CompareCommand.COMMAND_NAME, 
                    opt.getValueAt(0), opt.getValueAt(1));
            repl.shutdown();
            System.exit(0);
        }


        Environment env = Environment.getInstance();
        try {
            int records = env.getDatabase().getRecordCount();
            if (records <= 0){
                System.err.println("WARNING: Victims database is empty!"
                        + " Run command again with the "
                        + SYNC_FLAG + " flag.");
            }
        } catch (VictimsException e){
            System.err.println(e.getMessage());
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

        int rc = CommandResult.RESULT_SUCCESS;
        for (CommandResult result : repl.completedCommands()){
            repl.print(result);
            if (result != null && (result.failed() || result instanceof ExitTerminate)){
                rc = result.getResultCode();
                break;
            }
        }
        repl.shutdown();
        System.exit(rc);

    }
}
