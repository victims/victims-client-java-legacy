package pomParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pom {
	
	@Override
	public String toString() {
		return "Pom [dependencies=" + dependencies + "]";
	}

	private ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
	private Map<String, String> properties = new HashMap<String, String>();
	private String location = new String();
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public ArrayList<Dependency> getDependancies() {
		return dependencies;
	}

	public void setDependancies(ArrayList<Dependency> dependancies) {
		this.dependencies = dependancies;
	}
	
	public void addDependency(Dependency dependency){
		if(!dependencies.contains(dependency)){
			this.dependencies.add(dependency);
		}
	}
	
	
	
}
