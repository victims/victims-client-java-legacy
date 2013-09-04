package pomParser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class AllTests {
	String localPath = System.getProperty("user.dir");
	
	@Test
	public void checkNoOfDependancies() throws IOException{
		Parser parser = new Parser();
		Pom pom = parser.parsePom(localPath+"//src//main//java//testing//testJGAPPom.xml", new HashMap<String,String>());
		ArrayList<Dependency> deps = pom.getDependancies();
		if(deps.size() != 14){
			fail("Expected 14 dependancies, found "+deps.size());
		}
		return;
	}
	
	@Test
	public void PomUtilsResolveName(){
		Dependency dep = new Dependency("log4j","log4j","1.2.9","","","","");
		String resolvedName = PomUtils.resolveName(dep,"-");
		String shouldBe = "log4j-log4j-1.2.9";
		if(!resolvedName.equals(shouldBe)){
			fail("resolved name should be: "+resolvedName);
		}
	}
	
	@Test
	public void PomUtilsResolveName1(){
		Dependency dep = new Dependency("log4j","log4j","","","","","");
		String resolvedName = PomUtils.resolveName(dep,"-");
		String shouldBe = "log4j-log4j";
		if(!resolvedName.equals(shouldBe)){
			fail("resolved name should be: "+resolvedName);
		}
	}
	
	@Test 
	public void PomUtilsResolveDependancyLocation1(){
		Dependency dep = new Dependency("commons-logging","commons-logging-api","1.0.4","","","","");
		String resolvedLocation = PomUtils.resolveDependencyLocation(dep);
		String shouldBe = System.getenv("HOMEDRIVE")+System.getenv("HOMEPATH")+"\\.m2\\repository\\commons-logging\\commons-logging-api\\1.0.4";
		if(!resolvedLocation.equals(shouldBe)){
			fail("resolved directory, "+resolvedLocation+", does not equal: "+shouldBe);
		}
	}
	
	@Test 
	public void PomUtilsResolveDependancyLocation2(){
		Dependency dep = new Dependency("non","existant","","","","","");
		File newDir = new File(PomUtils.resolveDependencyLocation(dep));
		if(!newDir.exists()){
			fail("The directory should exist as it was just made...");
			newDir.delete();
		}
	}
	
	@Test
	public void checkDependancy() throws IOException{
		Parser parser = new Parser();
		Pom pom = parser.parsePom(localPath+"//src//main//java//testing//testJGAPPom.xml", new HashMap<String,String>());
		ArrayList<Dependency> deps = pom.getDependancies();
		Dependency dep = deps.get(0);
		if(!dep.getName().equals("commons-lang,commons-lang-2.3")){
			fail("Expecting the first dependancies name to be: commons-lang,commons-lang-2.3, got: "+dep.getName());
		}
		return;
	}
	
	@Test
	public void checkDependancyWithSystemPath() throws IOException{
		Parser parser = new Parser();
		Pom pom = parser.parsePom(localPath+"//src//main//java//testing//testJGAPPom.xml", new HashMap<String,String>());
		ArrayList<Dependency> deps = pom.getDependancies();
		Dependency dep = deps.get(4);
		System.err.println(dep.getName());
		System.err.println(dep.getSystemPath());
		if(!dep.getSystemPath().equals("")){
			//fail("Expecting the first dependancies name to be: commons-lang,commons-lang-2.3, got: "+dep.getName());
		}
		return;
	}
	
	@Test
	public void checkPom() throws IOException{
		Parser parser = new Parser();
		Pom pom = parser.parsePom(localPath+"//src//main//java//testing//testJGAPPom.xml", new HashMap<String,String>());
		if(!pom.getProperties().equals("{project.modelVersion=4.0.0, project.version=3.6, project.name=JGAP, project.groupid=net.sf.jgap, project.artifactid=jgap}")){
			fail("expecting the properties of pom to be: {project.modelVersion=4.0.0, project.version=3.6, project.name=JGAP, project.groupid=net.sf.jgap, project.artifactid=jgap}, got: "+pom.getProperties());
		}
		return;
	}
	
}
