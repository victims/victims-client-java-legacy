package pomParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {
	private Map<String, String> properties = new HashMap<String, String>();

	/**
	 * @param args
	 * @return 
	 * @throws IOException
	 */
	public Pom parsePom(String filename,Map<String, String> properties) throws IOException  {
		if(properties.size() > 0){
			this.setProperties(properties);
		}
		File input = new File(filename);
		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		Pom superPom = new Pom(); 
		getParentProperties(doc);
		getPropertyProperties(doc);
		getProjectProperties(doc);
		superPom.setProperties(this.properties);
		superPom.setLocation(filename);
		Elements dependency = doc.select("dependency");
		for (Element elem : dependency) {
			String groupId = "";
			String artifactId = "";
			String version = "";
			String classifier = "";
			String type = "";
			String scope = "";
			String systemPath = "";
			for (Element elemChild : elem.children()) {
				System.err.println(elemChild);
				if (elemChild.nodeName().equals("groupid")) {
					groupId = susbtituteVariables(elemChild.html());
				} else if (elemChild.nodeName().equals("artifactid")) {
					artifactId = susbtituteVariables(elemChild.html());
				} else if (elemChild.nodeName().equals("version")) {
					version = susbtituteVariables(elemChild.html());
				}else if (elemChild.nodeName().equals("classifier")) {
					classifier = susbtituteVariables(elemChild.html());
				}else if (elemChild.nodeName().equals("type")) {
					type = susbtituteVariables(elemChild.html());
				}else if (elemChild.nodeName().equals("scope")) {
					scope = susbtituteVariables(elemChild.html());
				}else if (elemChild.nodeName().equals("systemPath")) {
					System.err.println("system path found: "+groupId);
					systemPath = susbtituteVariables(elemChild.html());
				}
			}
			superPom.addDependency(new Dependency(groupId,artifactId,version,classifier,type,scope,systemPath));
		}
		return superPom;
	}

	private String susbtituteVariables(String property) {
		StringBuilder output = new StringBuilder();
		String[] split = property.split("\\$");
		int start = -1;
		int end = -1;
		for (String elem : split) {
			start = elem.indexOf("{");
			end = elem.indexOf("}");
			if (start == -1 || end == -1) {
				output.append(elem);
				continue;
			}
			String key = elem.substring(start + 1,end);
			
			if(key.startsWith("env.")){
				output.append(elem.replace(elem.substring(start,end+1),System.getenv(key.substring(4))));
			}else if(System.getProperties().containsKey(key)){
				output.append(elem.replace(elem.substring(start,end+1),System.getProperties().get(key).toString()));
			}else if(this.properties.containsKey(key)){
				output.append(elem.replace(elem.substring(start,end+1),this.properties.get(key)));
			}
		}
		return output.toString();
	}

	private void getParentProperties(Document doc) {
		Elements elements = doc.select("parent");
		for (Element elem : elements) {
			for (Element elemChild : elem.children()) {
				this.properties.put(elemChild.nodeName().toLowerCase(), elemChild.html());
			}
		}
	}

	private void getPropertyProperties(Document doc) {
		Elements elements = doc.select("properties");
		for (Element elem : elements) {
			for (Element elemChild : elem.children()) {
				this.properties.put(elemChild.nodeName().toLowerCase(), elemChild.html());
			}
		}
	}
	private void getProjectProperties(Document doc){
		Elements project = doc.select("project");
		if(project.select("artifactId").size() > 0)this.properties.put("project.artifactid",project.select("artifactId").first().html());
		if(project.select("artifactId").size() > 0)this.properties.put("project.artifactid", project.select("artifactId").first().html());
		if(project.select("groupId").size() > 0)this.properties.put("project.groupid",project.select("groupId").first().html());
		if(project.select("version").size() > 0)this.properties.put("project.version",project.select("version").first().html());
		if(project.select("relativePath").size() > 0)this.properties.put("project.relativepath",project.select("relativePath").first().html());
		if(project.select("package").size() > 0)this.properties.put("project.package",project.select("package").first().html());
		if(project.select("name").size() > 0)this.properties.put("project.name",project.select("name").first().html());
		if(project.select("modelVersion").size() > 0)this.properties.put("project.modelVersion",project.select("modelVersion").first().html());
		
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
