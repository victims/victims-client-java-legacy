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
		
		// check path exists
		Parser parser = new Parser();
		Pom first = parser.parsePom(pomLocation, new HashMap<String, String>());
		handleDependancy(first);
		System.err.println("No of dependancies: " + depLocation.size());
		VictimsFileDB vdb = VictimsCLI.intializeDB();
		for (String dependencyLocation : depLocation) {
			try{
				vdb.scan(dependencyLocation);
			}catch(Exception e){
				//TODO log
				e.printStackTrace();
			}
		}

	}

	private static void handleDependancy(Pom pom) throws IOException {
		 //System.err.println("Current Pom: "+pom.getLocation());
		for (Dependency dep : pom.getDependancies()) {
			String pomFile = new String();
			String jarFile = new String();
			String fileExtension = new String();
			String resolvedLocation = PomUtils.resolveDependencyLocation(dep);

			if (dep.getVersion().equals("")) {
				dep.setVersion(DownloadUtils.checkMavenForLatestVersion(dep));
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
					newPom = parser.parsePom(pomFile, pom.getProperties());
				} catch (IOException e) {
					DownloadUtils.downloadFileFromCentralRepo(PomUtils.resolveName(dep, "/") + "/" + fileExtension, "pom", pomFile);
					continue;
				}
			}
			if (!depLocation.contains(jarFile)) {
				File jar = new File(jarFile);
				if (jar.exists()) {
					//TODO log
				} else {
					if(DownloadUtils.downloadFileFromCentralRepo(PomUtils.resolveName(dep, "/") + "/" + fileExtension, "jar", jarFile) == false){
						//TODO log
						System.err.println("jar failed to download: "+PomUtils.resolveName(dep, "/") + "/" + fileExtension+".jar");
					}
				}
				depLocation.add(jarFile);
				handleDependancy(newPom);
			} else {
				return;
			}
		}

	}

}
