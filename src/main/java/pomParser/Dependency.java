package pomParser;

public class Dependency {
	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;
	private String type;
	private String scope;
	private String systemPath;
	private boolean retrieved;
	
	public Dependency(String groupId, String artifactId, String version,
			String classifier, String type, String scope, String systemPath) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.type = type;
		this.scope = scope;
		this.systemPath = systemPath;
		this.retrieved = false;
	}

	@Override
	public String toString() {
		return "Dependency [groupId=" + groupId + ", artifactId=" + artifactId
				+ ", version=" + version + "]";
	}

	public boolean isRetrieved() {
		return retrieved;
	}

	public void setRetrieved(boolean retrieved) {
		this.retrieved = retrieved;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getSystemPath() {
		return systemPath;
	}

	public void setSystemPath(String systemPath) {
		this.systemPath = systemPath;
	}
	

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
