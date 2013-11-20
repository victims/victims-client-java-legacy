
package com.redhat.victims.cli.commands;

/*
 * #%L
 * This file is part of victims-client.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 * @author gm
 */
public class PomScannerCommand implements Command {
    
    private Usage help; 
    public PomScannerCommand(){ 
        help = new Usage(getName(), "Scan dependencies in a pom.xml file");
        help.addExample("pom.xml");
        
    }

    @Override
    public final String getName() {
        return "scan-pom";
    }

    private String scanPomFile(File pomFile){
        
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        StringBuilder sb = new StringBuilder();
        try { 
            
            VictimsDBInterface db = VictimsDB.db();
            Model model = pomReader.read(new FileReader(pomFile));
            for (Dependency dep : model.getDependencies()){
                HashMap<String, String> gav = new HashMap();
                String groupId = dep.getGroupId();
                String artifactId = dep.getArtifactId();
                String version = dep.getVersion();
                String info = String.format("%s, %s, %s", groupId, artifactId, version);
                
                gav.put("groupId", groupId);
                gav.put("artifactId", artifactId);
                gav.put("version", version);
                
                          
                HashSet<String> cves = db.getVulnerabilities(gav);
                if (! cves.isEmpty()){
                    sb.append(info);
                    sb.append(" VULNERABLE! ");
                    for (String cve : cves){
                        sb.append(cve);
                        sb.append(" ");
                    }
                } else {
                    sb.append(info);
                    sb.append(" ok ");
                }
                sb.append(String.format("%n"));
            }
            return sb.toString();
            
            
        } catch (IOException e){
            return String.format("error: %s", e.getMessage());
            
        } catch (XmlPullParserException e) {
            return String.format("error: malformed POM file '%s'", 
                    pomFile.getAbsolutePath());
            
        } catch (VictimsException e){ 
            return String.format("error: %s", e.getMessage());
        }
        
    }
    
    
    @Override
    public CommandResult execute(List<String> args) {
        
        StringBuilder sb = new StringBuilder();
        
        for (String arg : args){
            File pomFile = new File(arg);
            sb.append("scanning - ");
            sb.append(arg);
            sb.append(String.format("%n"));
            if (! pomFile.exists()){
                sb.append(String.format("no such file: %s", arg));               
            } else {
                sb.append(scanPomFile(pomFile));
            }
            sb.append(String.format("%n"));

        }
        
        return new ExitSuccess(sb.toString());
        
    }

    @Override
    public String usage() {
        return help.toString();
        
    }
    
}
