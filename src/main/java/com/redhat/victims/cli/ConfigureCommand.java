package com.redhat.victims.cli;


import com.redhat.victims.cli.Command;
import com.redhat.victims.cli.TUI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gm
 */
public class ConfigureCommand implements Command {

    final static List<String> settings;
    
    static {
        List<String> keys = new ArrayList();
        keys.add("victims.service.uri"); 
        keys.add("victims.service.entry"); 
        keys.add("victims.encoding"); 
        keys.add("victims.home"); 
        keys.add("victims.cache.purge"); 
        keys.add("victims.algorithms"); 
        keys.add("victims.db.driver"); 
        keys.add("victims.db.url"); 
        keys.add("victims.db.user"); 
        keys.add("victims.db.pass");
        keys.add("victims.db.purge");
        settings = Collections.unmodifiableList(keys);
    };
    
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String execute(List<String> args) {
        if (args == null || args.isEmpty()){
            return null;
        }
        
        String subCommand = args.get(0);
        
        // list 
        if (subCommand.equalsIgnoreCase("list")){
            
            StringBuilder sb = new StringBuilder();
            for (String setting : settings){
                sb.append(setting); 
                sb.append(" = ");
                sb.append(System.getProperty(setting));
                sb.append(String.format("%n"));
            }
            return sb.toString();
            
        // get
        } else if (subCommand.equalsIgnoreCase("get")){
            if (args.size() == 2 && settings.contains(args.get(1))){
                String val = System.getProperty(args.get(1));
                if (val == null){
                    val = "(not set)";
                }
                return val;
                
            } else {
                return "invalid setting";
            }
            
        // set
        } else if (subCommand.equalsIgnoreCase("set")){
            if (args.size() == 3 && settings.contains(args.get(1))){
                String old = System.setProperty(args.get(1), args.get(2));
                if (old == null){
                    old = "(not set)";
                }
                return String.format("changed %s from %s to %s", 
                        args.get(1), old, args.get(2));
            } else {
                return "invalid setting";
            }
        } else {
            return "get, set, or list expected";
        }
        
    }

    @Override
    public String usage() {
        ArrayList<String> usecases = new ArrayList();
        usecases.add("list");
        usecases.add("get victims.home");
        usecases.add("set victims.home /home/user/example");
               
        return TUI.formatUsage(getName(), 
                "list, sets or gets configuration options for the victims client", 
                usecases.toArray(new String[usecases.size()]));
 
    }
}
