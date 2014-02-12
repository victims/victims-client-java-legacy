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

import com.redhat.victims.cli.Repl;
import com.redhat.victims.cli.results.ExitInvalid;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitSuccess;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gm
 */
public class MapCommand implements Command {
    
    public static final String COMMAND_NAME = "map";
    
    private Usage help;
    private List<String> arguments; 
    private Repl repl;
    
    public MapCommand(Repl repl) {
        this.repl = repl;
        
        help = new Usage(getName(), "Asynchronously maps a command to each input argument");
        help.addExample("scan-file file1.jar file2.jar file3.jar file4.jar");
        
    }
    
    @Override
    public CommandResult execute(List<String> args){
        
        if (args  == null || args.size() <= 1){
            return new ExitInvalid("invalid arguments");
        }
        
        String key = args.remove(0);
        for (String arg : args){
        	repl.runCommand(key, arg);
        }
               
        ExitSuccess rc = new ExitSuccess(null);
        rc.addVerboseOutput(String.format("Submitted %d %s tasks", args.size(), key));
        return rc;
        
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
    public final String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String usage() {
        return help.toString();
    }
    
    @Override
    public Command newInstance(){
        MapCommand inst = new MapCommand(this.repl);
        inst.setArguments(this.arguments);
        return inst;
    }
    
}

