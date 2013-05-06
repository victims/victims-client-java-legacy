package com.redhat.victims;

import java.io.IOException;

public class VictimsCLI {
	public static void main(String[] args) throws IOException {
		for (String arg : args) {
			VictimsScanner.scan(arg, System.out);
		}
	}
}

