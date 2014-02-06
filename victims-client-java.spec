Name:           victims-client-java
Version:        0.alpha.0.1
Release:        1%{?dist}
Summary:        Java client for the Victims Project
Group:		Development/Tools
BuildArch:	noarch

License:        AGPLv3+
URL:            https://github.com/victims/victims-client-java
Source0:        https://github.com/victims/victims-client-java/archive/alpha-0.1.tar.gz

BuildRequires:	maven-local
BuildRequires:	victims-lib-java
BuildRequires:	plexus-utils
BuildRequires:	javapackages-tools

Requires:	victims-lib-java
Requires:	plexus-utils
Requires:	maven-model

%description
The victims java client will scan any supplied pom.xml, jar files or
directories supplied as command line arguments.

%package javadoc
Summary:        Javadocs for %{name}
Group:          Documentation

%description javadoc
This package contains the API documentation for %{name}.

%prep
%autosetup -p1
# Do not bundle dependencies in the generated JAR file.
%pom_xpath_inject "/pom:project/pom:dependencies/pom:dependency[./pom:artifactId = 'victims-lib' or ./pom:artifactId = 'maven-model']" "<scope>provided</scope>"

%build
%mvn_build

%install
%mvn_install
%jpackage_script com.redhat.victims.cli.Main "" "" victims-client-java:victims-lib-java:plexus-utils:apache-commons-io:h2:google-gson:apache-commons-codec:apache-commons-lang:maven-model victims-client-java

%files -f .mfiles
%{_bindir}/victims-client-java
%dir %{_javadir}/%{name}

%files javadoc -f .mfiles-javadoc

%changelog
* Tue Feb  4 2014 Florian Weimer <fweimer@redhat.com> - 0.alpha.0.1-1
- Initial packaging
