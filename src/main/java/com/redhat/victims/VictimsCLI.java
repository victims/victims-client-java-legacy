package com.redhat.victims;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.redhat.victims.fingerprint.Artifact;
import com.redhat.victims.fingerprint.Processor;

public class VictimsCLI {

	/**
	 * 
	 * @param file
	 * @param os
	 * @throws IOException
	 */
	private static void processFile(File file, OutputStream os)
			throws IOException {
		File f = file;
		String path = f.getAbsolutePath();
		Artifact artifact = Processor.process(path);
		VictimsRecord vr = new VictimsRecord(artifact);
		String line = vr.toString();
		line += "\n";
		os.write(line.getBytes());
	}

	/**
	 * 
	 * @param dir
	 * @param os
	 * @throws IOException
	 */
	private static void processDir(File dir, OutputStream os)
			throws IOException {
		Collection<File> files = FileUtils.listFiles(dir, new RegexFileFilter(
				"^(.*?)\\.jar"), DirectoryFileFilter.DIRECTORY);
		Iterator<File> fi = files.iterator();
		while (fi.hasNext()) {
			processFile(fi.next(), os);
		}
	}

	/**
	 * 
	 * @param source
	 * @param os
	 * @throws IOException
	 */
	public static void process(String source, OutputStream os)
			throws IOException {
		File f = new File(source);
		if (f.isDirectory()) {
			processDir(f, os);
		} else if (f.isFile()) {
			processFile(f, os);
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		for (String arg : args) {
			process(arg, System.out);
		}
	}

}
