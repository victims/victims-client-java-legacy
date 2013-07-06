package pomParser;

import java.io.File;

public class PomUtils {
	
	public static String resolveDependencyLocation(Dependency dep){
		String dir = System.getenv("HOMEDRIVE")+System.getenv("HOMEPATH")+"\\.M2\\repository\\"+resolveName(dep,"\\");		
		File directory = new File(dir);
		if(!directory.exists()){
			/**
			 * TODO
			 * check to see if the system path is set 
			 * use that to get the location
			 * if system path isn't set then download the repo
			 */
			System.err.println("Directory doesn't exist: "+dir);
			System.err.println("Downloading dependancy: "+dep.getGroupId()+"-"+dep.getArtifactId()+"-"+dep.getVersion());
			System.err.println("Done...Not really");
			/*
			 * two options
			 * make one
			 * check to see if this is really where the m2 directory is
			 */
			directory.mkdir();
		}
			return dir;
	}
	public static String resolveName(Dependency dep,String delimeter){
		String name;
		if(dep.getVersion().equals("")){
			name = dep.getGroupId().replace(".", delimeter)+delimeter+dep.getArtifactId();
		}else{
			name = dep.getGroupId().replace(".", delimeter)+delimeter+dep.getArtifactId()+delimeter+dep.getVersion();
		}
		return name;
	}
	
}
