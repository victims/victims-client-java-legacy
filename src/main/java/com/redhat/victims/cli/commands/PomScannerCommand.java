
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
import com.redhat.victims.cli.results.CommandResult;
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
    private List<String> arguments; 

    public PomScannerCommand(){ 
        help = new Usage(getName(), "Scan dependencies in a pom.xml file");
        help.addExample("pom.xml");
        
    }

    @Override
    public final String getName() {
        return "scan-pom";
    }

    private void scanPomFile(File pomFile, CommandResult result){
        
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
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
                    
                    result.addOutput(info);
                    result.addOutput(" VULNERABLE! ");
                    for (String cve : cves){
                        result.addOutput(cve);
                        result.addOutput(" ");
                    }
                    result.addOutput(String.format("%n"));
                    
                } else {
                    result.addVerboseOutput(info);
                    result.addVerboseOutput(" ok ");
                }
                
       
            }
           
        } catch (IOException e){
            result.setResultCode(CommandResult.RESULT_ERROR);
            result.addOutput(String.format("error: %s", e.getMessage()));
            
        } catch (XmlPullParserException e) {
            result.setResultCode(CommandResult.RESULT_ERROR);
            result.addOutput(String.format("error: malformed POM file '%s'", 
                    pomFile.getAbsolutePath()));
            
        } catch (VictimsException e){ 
            result.setResultCode(CommandResult.RESULT_ERROR);
            result.addOutput(String.format("error: %s", e.getMessage()));
        }
        
    }
    
    
    @Override
    public CommandResult execute(List<String> args) {
        
        CommandResult result = new CommandResult();
        result.setResultCode(CommandResult.RESULT_SUCCESS);
        
        for (String arg : args){
            File pomFile = new File(arg);
            result.addVerboseOutput("scanning - ");
            result.addVerboseOutput(arg);
            result.addVerboseOutput(String.format("%n"));
            if (! pomFile.exists()){
                result.addOutput(String.format("no such file: %s%n", arg));               
            } else {
                scanPomFile(pomFile, result);
            }
        }
        
        return result;
        
    }

    @Override
    public String usage() {
        return help.toString();
        
    }
    
    @Override
    public void setArguments(List<String> args) {
        this.arguments = args;
    }

    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);
    }
    
    @Override
    public Command newInstance(){
        return new PomScannerCommand(); 
    }
    
}
