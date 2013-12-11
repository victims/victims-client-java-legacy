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



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.redhat.victims.cli.results.*;

/**
 *
 * @author gm
 */
public class ConfigureCommand implements Command {

    
    public static final String COMMAND_NAME = "config";

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
    private List<String> arguments; 
    
    public ConfigureCommand(){
     
      help = new Usage(getName(), "List, sets or gets configuration options for the victims client");
      help.addExample("list");
      help.addExample("get victims.home");
      help.addExample("set victims.home /home/user/example");
    }
    
    @Override
    public final String getName() {
        return COMMAND_NAME;
    }
    
    private CommandResult list(){
      
      CommandResult result = new CommandResult();
      for (String setting : settings) {
        result.addOutput(setting);
        result.addOutput(" = ");
        result.addOutput(System.getProperty(setting));
        result.addOutput(String.format("%n"));
      }
      result.setResultCode(CommandResult.RESULT_SUCCESS);
      return result;
    }
    
    private CommandResult get(List<String> args){
      
      if (args.size() == 2 && settings.contains(args.get(1))) {
        String val = System.getProperty(args.get(1));
        if (val == null) {
          val = "(not set)";
        }
        return new ExitSuccess(val);

      } else {
        return new ExitInvalid("invalid setting");
      }
    }
    
    public CommandResult set(List<String> args){
      
      if (args.size() == 3 && settings.contains(args.get(1))) {
        String old = System.setProperty(args.get(1), args.get(2));
        ExitSuccess result = new ExitSuccess(null);
        result.addVerboseOutput(args.get(1));
        result.addVerboseOutput(" ");
        result.addVerboseOutput(old); 
        result.addVerboseOutput(" => ");
        result.addVerboseOutput(args.get(2));

        return result;

      } else {
        return new ExitInvalid("invalid setting");
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
            return new ExitInvalid("get, set, or list expected");
        }      
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
        ConfigureCommand cmd = new ConfigureCommand();
        return new ConfigureCommand();
    }
}
