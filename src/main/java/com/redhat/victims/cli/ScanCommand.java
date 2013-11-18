
package com.redhat.victims.cli;

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

    @Override
    public String getName() {
        return "scan";
    }

    @Override
    public String execute(List<String> args) {
        if (args == null){
            return "file or directory expected";
        }
        
        VictimsDBInterface db; 
        try {
           db = VictimsDB.db();
        } catch (VictimsException e){
            return String.format("error: %s", e.getMessage());
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
                    }
                }
            } catch (IOException e){
                sb.append(String.format("error: %s%n", e.getMessage()));
            }
        }
        if (sb.length() > 0){
            return sb.toString();
        } 
        return TUI.SUCCESS;
    }

    @Override
    public String usage() {
        return TUI.formatUsage(getName(), 
                "scans the supplied .jar file and reports any vulnerabilities", 
                "path/to/file.jar");
    }
    
}
