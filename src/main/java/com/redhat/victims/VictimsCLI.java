package com.redhat.victims;

import java.io.IOException;

public class VictimsCLI {

	/**
 * 	 * @param args
 * 	 	 * @throws IOException
 * 	 	 	 */
	public static void main(String[] args) throws IOException {
		for (String arg : args) {
			VictimsScanner.scan(arg, System.out);
		}
	}

}

