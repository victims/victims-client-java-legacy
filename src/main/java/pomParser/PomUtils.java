package pomParser;

import java.io.File;

public class PomUtils {
	
	public static String resolveDependencyLocation(Dependency dep){
		String dir = System.getenv("HOMEDRIVE")+System.getenv("HOMEPATH")+"\\.m2\\repository\\"+resolveName(dep,"\\");		
		File directory = new File(dir);
		if(!directory.exists()){
			System.out.println("Directory doesn't exist: "+dir);
			
			if(directory.mkdirs()){
				System.out.println("directory created...");
			}else{
				System.out.println("directory cannot be created...");
			}
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
