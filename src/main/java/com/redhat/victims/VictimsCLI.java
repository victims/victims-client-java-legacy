package com.redhat.victims;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;

public class VictimsCLI extends AbstractCLI {

	public VictimsCLI(String[] args) throws IOException {
		super("VictimsCLI", args);
	}

	public void run(CommandLine line, OutputStream os) throws IOException {
		System.setProperty("jsse.enableSNIExtension", "false");
		
		VictimsFileDB vdb = intializeDB();
		
		// Scan given files/dir
		for (String arg : line.getArgs()) {
			System.out.println("Scanning: " + arg);
			try {
				vdb.scan(arg);
			} catch (IOException e) {
				System.out.println("Failed to scan " + arg + " : "
						+ e.getMessage());
			}
			System.out.println("Scanning Complete: " + arg);
		}
	}

	public static VictimsFileDB intializeDB(){
		// initialize FileDB
				VictimsFileDB vdb = new VictimsFileDB();
				try {
					System.out.println("Synchronizing database with web service.");
					vdb.sync();
					System.out.println("Sync complete.");
				} catch (Exception e) {
					System.out.println("Could not sync with service.");
				}
				return vdb;
	}
	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
		VictimsCLI scanner = new VictimsCLI(args);
	}

}
