package pomParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.redhat.victims.VictimsCLI;
import com.redhat.victims.VictimsFileDB;

public class Overseer {
	static Parser parser = new Parser();
	static ArrayList<String> depLocation = new ArrayList<String>();
	static ArrayList<String> processedPoms = new ArrayList<String>();

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void scanPom(String pomLocation) throws IOException {
		 //DownloaderProxyAuthenticator.setupProxy();
		// check path exists
		Parser parser = new Parser();
		Pom first = parser.parsePom(pomLocation, new HashMap<String, String>());
		handleDependancy(first);
		System.out.println("No of dependancies: " + depLocation.size());
		VictimsFileDB vdb = VictimsCLI.intializeDB();
		for (String dependencyLocation : depLocation) {
			try {
				vdb.scan(dependencyLocation);
			} catch (Exception e) {
				System.err.println("error scanning dependency - " + dependencyLocation);
				e.printStackTrace();
			}
		}
	}

	private static void handleDependancy(Pom pom) throws IOException {
		for (Dependency dep : pom.getDependancies()) {
			if (!dep.getSystemPath().equals("")) {
				System.err.println(dep.getName() + ":  " + dep.getSystemPath());
			}
			String pomFile = new String();
			String jarFile = new String();
			String fileExtension = new String();
			String resolvedLocation = PomUtils.resolveDependencyLocation(dep);
			if (dep.getVersion().equals("")) {
				String version = DownloadUtils.checkMavenForLatestVersion(dep);
				if (version.equals("")) {
					System.out.println("No version of " + dep.getArtifactId() + " was found... and could not be retrieved");
					continue;
				} else {
					dep.setVersion(version);
				}
			}
			if (resolvedLocation.equals("")) {
				return;
			}
			if (dep.getVersion().equals("")) {
				fileExtension = dep.getArtifactId();
			} else {
				fileExtension = dep.getArtifactId() + "-" + dep.getVersion();
			}
			pomFile = resolvedLocation + "\\" + fileExtension + ".pom";
			jarFile = resolvedLocation + "\\" + fileExtension + ".jar";
			Pom newPom = null;
			if (!processedPoms.contains(pomFile)) {
				processedPoms.add(pomFile);
				try {
					if (new File(pomFile).canRead()) {
						newPom = parser.parsePom(pomFile, pom.getProperties());
					} else {
						if (DownloadUtils.downloadFileFromCentralRepo(PomUtils.resolveName(dep, "/") + "/" + fileExtension, "pom", pomFile)) {
							newPom = parser.parsePom(pomFile, pom.getProperties());
						} else {
							continue;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (!dep.getSystemPath().equals("")) {
				addDependancy(dep.getSystemPath(), dep, fileExtension, newPom);
			} else {
				addDependancy(jarFile, dep, fileExtension, newPom);
			}
		}
	}

	private static void addDependancy(String jarFile, Dependency dep, String fileExtension, Pom newPom) throws IOException {
		if (!depLocation.contains(jarFile)) {
			File jar = new File(jarFile);
			if (!jar.exists()) {
				if (DownloadUtils.downloadFileFromCentralRepo(PomUtils.resolveName(dep, "/") + "/" + fileExtension, "jar", jarFile) == false) {
					System.out.println("jar failed to download: " + PomUtils.resolveName(dep, "/") + "/" + fileExtension + ".jar");
				} else {
					System.out.println("jar download successful: " + PomUtils.resolveName(dep, "/") + "/" + fileExtension + ".jar");
				}
			}
			System.out.println("jar File: " + jarFile);
			depLocation.add(jarFile);
			handleDependancy(newPom);
		} else {
			return;
		}
	}

}
