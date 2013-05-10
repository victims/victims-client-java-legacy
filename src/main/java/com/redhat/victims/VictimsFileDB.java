package com.redhat.victims;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.redhat.victims.VictimsService.RecordStream;
import com.redhat.victims.fingerprint.Algorithms;

public class VictimsFileDB {
	private static final String CACHE_DIR = ".victims-client-cache";
	private static final String DB_ROOT = "db";
	private static final String LAST_UPDATED_FILE = ".last-updated";
	private static final String CVE_KEY = "cves";

	protected File dbroot;
	protected File cache;
	protected File lastUpdate;
	protected VictimsService service;
	protected EntryCache entryCache;

	private static File getDir(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	private static String mergePath(File parent, String child) {
		return String.format("%s%s%s", parent.getAbsoluteFile(),
				File.separator, child);
	}

	public VictimsFileDB(String cacheDir) {
		this.cache = getDir(cacheDir);
		this.dbroot = getDir(mergePath(cache, DB_ROOT));
		this.service = new VictimsService();
		this.lastUpdate = new File(mergePath(cache, LAST_UPDATED_FILE));
		this.entryCache = new EntryCache();
	}

	public VictimsFileDB() {
		this(CACHE_DIR);
	}

	private File getEntryDir(String sha512) {
		return getDir(mergePath(dbroot, sha512));
	}

	private boolean remove(VictimsRecord vr) {
		File dir = getEntryDir(vr.hash);
		if (dir.exists()) {
			return dir.delete();
		}
		return true;
	}

	private boolean persist(VictimsRecord vr) {
		File dir = getEntryDir(vr.hash);
		try {
			// persists hashes
			for (String alg : vr.hashes.keySet()) {
				if (alg.equals(VictimsRecord.normalizeKey(Algorithms.SHA512))) {
					FileOutputStream hashes = new FileOutputStream(mergePath(
							dir, alg));
					for (String hash : vr.hashes.get(alg).keySet()) {
						hashes.write(String.format("%s\n", hash).getBytes(
								VictimsConfig.charset()));
					}
					hashes.close();
				}
			}

			// persist cves
			FileOutputStream cves = new FileOutputStream(
					mergePath(dir, CVE_KEY));
			for (String cve : vr.cves) {
				cves.write(String.format("%s\n", cve).getBytes(
						VictimsConfig.charset()));
			}
			cves.close();
			return true;
		} catch (IOException e) {
			if (dir.exists()) {
				// All or nothing deal
				dir.delete();
			}
			System.out.println(String.format("Failed to persist %s: %s",
					vr.hash, e.getMessage()));
		}
		return false;
	}

	public void sync() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(VictimsRecord.DATE_FORMAT);
		Date since = sdf.parse("1900-01-01T00:00:00");

		// get last updated if available
		try {
			if (lastUpdate.exists()) {
				String temp = FileUtils.readFileToString(lastUpdate).trim();
				since = sdf.parse(temp);
			}
		} catch (IOException e) {
		} catch (ParseException e) {
		}

		// fetch from service removes and then updates
		RecordStream rs;

		try {
			rs = service.removed(since);
			while (rs.hasNext()) {
				VictimsRecord vr = rs.getNext();
				remove(vr);
			}

			rs = service.updates(since);
			while (rs.hasNext()) {
				VictimsRecord vr = rs.getNext();
				persist(vr);
			}

			FileOutputStream fos = new FileOutputStream(lastUpdate);
			fos.write(sdf.format(new Date()).getBytes());
			fos.close();
		} catch (IOException e) {
			System.out.println("Failed to sync database: " + e.getMessage());
		}
	}

	private ArrayList<String> readLines(File file) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		List<String> list = FileUtils.readLines(file, VictimsConfig.charset());
		for (String line : list) {
			line = line.trim();
			if (!line.isEmpty()) {
				lines.add(line);
			}
		}
		return lines;
	}

	private Entry getEntry(String sha512) throws IOException {
		File dir = getEntryDir(sha512);
		String alg = VictimsRecord.normalizeKey(Algorithms.SHA512);
		Entry entry = (Entry) readLines(new File(mergePath(dir, alg)));
		return entry;
	}

	public ArrayList<String> getMatches(VictimsRecord vr) {
		File dir = getEntryDir(vr.hash);
		ArrayList<String> cves = new ArrayList<String>();
		if (dir.exists()) {
			try {
				cves = readLines(new File(mergePath(dir, CVE_KEY)));
			} catch (IOException e) {
				System.out.println("SHA-512 Matched, Cannot read CVE list: "
						+ e.getMessage());
			}
		} else {
			String[] directories = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			});
			for (String hash : directories) {
				hash = FilenameUtils.getBaseName(hash);
				Entry entry = new Entry();
				if (!entryCache.containsKey(hash)) {
					try {
						entryCache.put(hash, getEntry(hash));
					} catch (IOException e) {
						System.out.println("Could not load entry: "
								+ e.getMessage());
					}
				}

				entry = entryCache.get(hash);
				if (entry.isEmbeddedIn(vr)) {
					try {
						cves.addAll(readLines(new File(mergePath(dir, CVE_KEY))));
					} catch (IOException e) {
						System.out
								.println("SHA-512 Matched, Cannot read CVE list: "
										+ e.getMessage());
					}
				}
			}
		}
		return cves;
	}

	public void scan(String source) throws IOException {
		ArrayList<VictimsRecord> vrs = VictimsScanner.getRecords(source);
		for (VictimsRecord vr : vrs) {
			ArrayList<String> cves = getMatches(vr);
			if (cves.size() > 0) {
				System.out.println(String.format("%s:%s:%s matched %s",
						vr.name, vr.vendor, vr.version, cves.toString()));
			}
		}
	}

	@SuppressWarnings("serial")
	private static class EntryCache extends HashMap<String, Entry> {

	}

	@SuppressWarnings("serial")
	private static class Entry extends ArrayList<String> {

		public boolean isEmbeddedIn(VictimsRecord vr) {
			for (String hash : this) {
				if (!vr.getHashes(Algorithms.SHA512).containsKey(hash)) {
					return false;
				}
			}
			return true;
		}
	}
}
