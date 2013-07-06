package com.redhat.victims;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;

public class VictimsScannerCLI extends AbstractCLI {

	public VictimsScannerCLI(String[] args) throws IOException {
		super("VictimsScannerCLI", args);
	}

	public void run(CommandLine line, OutputStream os) throws IOException {
		System.err.println("ScannerCLI");
		for (String arg : line.getArgs()) {
			VictimsScanner.scan(arg, os);
		}
	}

	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
		VictimsScannerCLI scanner = new VictimsScannerCLI(args);
	}
}
