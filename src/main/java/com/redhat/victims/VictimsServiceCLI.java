package com.redhat.victims;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import pomParser.Overseer;

import com.redhat.victims.VictimsService.RecordStream;

public class VictimsServiceCLI extends AbstractCLI {

	public Options options() {
		Options options = super.options();
		options.addOption("s", "server", true, "use this as the victims server");
		options.addOption("u", "updates", false,
				"fetch updates from the server (incompatible with -r)");
		options.addOption("r", "removes", false,
				"fetch removals from the server (incompatible with -u)");
		options.addOption("d", "date", true,
				"date-time since which updates/removals are required (format:"
						+ VictimsRecord.DATE_FORMAT + ")");
		options.addOption("p", "pom file", true,
				"To scan the dependancies before the build, given the super pom");
		return options;
	}

	public void run(CommandLine line, OutputStream os) throws IOException {
		// service setup
		VictimsService service = null;
		if (line.hasOption("u") && line.hasOption("r")) {
			error("-u and -r cannot be used together");
		}

		if (line.hasOption("u") || line.hasOption("r")) {
			Date since = new Date();
			if (line.hasOption("d")) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						VictimsRecord.DATE_FORMAT);
				try {
					since = sdf.parse(line.getOptionValue("d"));
				} catch (java.text.ParseException e) {
					error("Cannot parse date."
							+ line.getOptionValue("d").trim());
				}
			} else {
				error("date not provided");
			}

			// we only need a service if we plan to update or remove
			if (line.hasOption("s")) {
				service = new VictimsService(line.getOptionValue("s"));
			} else {
				// use default uri
				service = new VictimsService();
			}

			RecordStream rs = null;
			if (line.hasOption("u")) {
				rs = service.updates(since);
			} else if (line.hasOption("r")) {
				rs = service.removed(since);
			}

			if (rs != null) {
				while (rs.hasNext()) {
					os.write(String.format("%s\n", rs.getNext().toString())
							.getBytes());
				}
			}
		}else if(line.hasOption("p")){
			//TODO scanning pom
			System.err.println("scanning "+line.getOptionValue("p"));
			Overseer.scanPom(line.getOptionValue("p"));
			
		}
	}

	public VictimsServiceCLI(String[] args) throws IOException {
		super("VictimsServiceCLI", args);
	}

	public static void main(String[] args) throws IOException {
		// TODO: Remove temporary fix once ssl is fixed
		System.setProperty("jsse.enableSNIExtension", "false");
		@SuppressWarnings("unused")
		VictimsServiceCLI service = new VictimsServiceCLI(args);

	}
}
