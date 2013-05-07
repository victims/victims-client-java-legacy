package com.redhat.victims;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public abstract class AbstractCLI {

	protected String COMMAND;
	protected Options options;

	public void help() {
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp(COMMAND, options);
		System.exit(0);
	}

	public void error(String message) {
		System.out.println("Error: " + message);
		help();
	}

	public Options options() {
		Options options = new Options();
		options.addOption("o", "output", true, "output to this file, "
				+ "if not provided standard-out will be used");
		options.addOption("h", "help", false, "print this message");
		return options;
	}

	public abstract void run(CommandLine line, OutputStream os)
			throws IOException;

	public void run(String[] args) throws IOException {
		CommandLineParser parser = new PosixParser();
		Options options = options();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("h")) {
				help();
			}

			// output setup
			OutputStream os = System.out;
			if (line.hasOption("o")) {
				try {
					File file = new File(line.getOptionValue("o"));
					os = new FileOutputStream(file);
				} catch (Exception e) {
					error(e.getMessage());
				}
			}
			this.run(line, os);
			if (os instanceof FileOutputStream) {
				os.flush();
				os.close();
			}
		} catch (ParseException e) {
			error(e.getMessage());
		}
	}

	public AbstractCLI(String command, String[] args) throws IOException {
		this.COMMAND = command;
		this.options = this.options();
		this.run(args);
	}

}
