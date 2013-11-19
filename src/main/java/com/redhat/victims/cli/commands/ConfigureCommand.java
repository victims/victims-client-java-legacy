package com.redhat.victims.cli.commands;



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
    
    private Usage help;
    
    public ConfigureCommand(){
     
      help = new Usage(getName(), "list, sets or gets configuration options for the victims client");
      help.addExample("list");
      help.addExample("get victims.home");
      help.addExample("set victims.home /home/user/example");
    }
    
    @Override
    public final String getName() {
        return "config";
    }
    
    private CommandResult list(){
      
      StringBuilder sb = new StringBuilder();
      for (String setting : settings) {
        sb.append(setting);
        sb.append(" = ");
        sb.append(System.getProperty(setting));
        sb.append(String.format("%n"));
      }
      return new ExitSuccess(sb.toString());
    }
    
    private CommandResult get(List<String> args){
      
      if (args.size() == 2 && settings.contains(args.get(1))) {
        String val = System.getProperty(args.get(1));
        if (val == null) {
          val = "(not set)";
        }
        return new ExitSuccess(val);

      } else {
        return new ExitFailure("invalid setting");
      }
    }
    
    public CommandResult set(List<String> args){
      
      if (args.size() == 3 && settings.contains(args.get(1))) {
        System.setProperty(args.get(1), args.get(2));
        return new ExitSuccess(null);

      } else {
        return new ExitFailure("invalid setting");
      }
    }

    @Override
    public CommandResult execute(List<String> args) {
      
        if (args == null || args.isEmpty()){
            return null;
        }
        
        String subCommand = args.get(0);
        
        if (subCommand.equalsIgnoreCase("list")){
            return list();
            
        } else if (subCommand.equalsIgnoreCase("get")){
            return get(args);
            
        } else if (subCommand.equalsIgnoreCase("set")){
            return set(args);
            
        } else {
            return new ExitFailure("get, set, or list expected");
        }      
    }

    @Override
    public String usage() {
        return help.toString();
    }
}
