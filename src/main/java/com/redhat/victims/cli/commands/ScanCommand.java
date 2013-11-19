
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
import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author gm
 */
public class ScanCommand implements Command {

    private Usage help;
    
    public ScanCommand(){
      help = new Usage(getName(), "scans the supplied .jar file and reports any vulnerabilities");
      help.addExample("path/to/file.jar");
      help.addExample("/directory/full/of/jars");
    }
  
    @Override
    public final String getName() {
        return "scan";
    }

    @Override
    public CommandResult execute(List<String> args) {
      
        if (args == null){
            return new ExitFailure("file or directory expected");
        }
        
        VictimsDBInterface db; 
        try {
           db = VictimsDB.db();
        } catch (VictimsException e){
            return new ExitFailure(e.getMessage());
        }
                
        StringBuilder sb = new StringBuilder(); 
        for (String arg : args){
            ArrayList<VictimsRecord> records = new ArrayList();
            try {
                VictimsScanner.scan(arg, records);
                for (VictimsRecord record : records){
                    
                    try{ 
                        HashSet<String> cves = db.getVulnerabilities(record);
                        if (! cves.isEmpty()){
                            sb.append(String.format("%s VULNERABLE! ", arg));
                            for (String cve : cves){
                                sb.append(cve);
                                sb.append(" ");
                            }
                        }
                    } catch(VictimsException e){
                      e.printStackTrace(System.err);
                      return new ExitFailure(e.getMessage());
                    }
                }
            } catch (IOException e){
                e.printStackTrace(System.err);
                return new ExitFailure(e.getMessage());
            }
        }
        if (sb.length() > 0){
          return new ExitSuccess(sb.toString());
        } 
        
        return new ExitSuccess("no vulnerabilities detected");
    }

    @Override
    public String usage() {
      return help.toString();
    }
    
}
