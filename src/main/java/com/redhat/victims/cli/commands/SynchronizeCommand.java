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
import com.redhat.victims.cli.results.ExitFailure;
import com.redhat.victims.cli.results.ExitSuccess;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.util.List;

/**
 * @author gm
 */
public class SynchronizeCommand implements Command {

    public static final String COMMAND_NAME = "sync";
    private Usage help;
    private List<String> arguments; 

    
    public SynchronizeCommand(){
      help = new Usage(getName(), "Update the victims database definitions");
      help.addExample("");
    }
    
    @Override
    public final String getName() {
        return COMMAND_NAME;
    }

    @Override
    public CommandResult execute(List<String> args) {
        try { 
            VictimsDBInterface db = VictimsDB.db();
            db.synchronize();
            ExitSuccess result = new ExitSuccess(null);
            result.addVerboseOutput("synchronization complete!");
            return result;
                    
        } catch (VictimsException e){
            //e.printStackTrace();
            return new ExitFailure(e.getMessage());
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
        return new SynchronizeCommand();

    }
    
}
