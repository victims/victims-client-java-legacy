package com.redhat.victims.cli.commands;

/*
 * #%L
 * This file is part of victims-client.
 * %%
 * Copyright (C) 2014 The Victims Project
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

import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitSuccess;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dfj
 */
public class CompareCommand implements Command {

    public static final String COMMAND_NAME = "compare";
    
    private Usage help;
    private List<String> arguments; 
    
    public CompareCommand(){
        help = new Usage(getName(), "Compare the hashes of two jar files");
        help.addExample("file1.jar,file2.jar");       
    }
    
    @Override
    public final String getName() {
        return COMMAND_NAME;
    }

    @Override
    public void setArguments(List<String> args) {
        this.arguments = args;
    }

    @Override
    public CommandResult execute(List<String> args) {
        
        CommandResult result = new ExitSuccess(null);
        
        for (String arg : args) {
            try {
                // Split argument string based on delimiter
                String[] jars = arg.split(",", 2);

                VictimsRecord vr1 = null;
                VictimsRecord vr2 = null;

                ArrayList<VictimsRecord> records = new ArrayList();
                VictimsScanner.scan(jars[0], records);
                for (VictimsRecord record : records){
                    vr1 = record;
                }

                VictimsScanner.scan(jars[1], records); 
                for (VictimsRecord record : records){
                    vr2 = record;
                }

                // Perform the comparison using .equals
                if (vr1.equals(vr2)){
                    result.addOutput("SUCCESS: " + jars[0] + " matches the hashed contents of " + jars[1]);
                } else {
                    result.addOutput("FAILURE: " + jars[0] + " does not match the hashed contents of " + jars[1]);
                }
            } catch (IOException e){
                result.addOutput(e.toString());
            }
        }
        return result;
    }

    @Override
    public String usage() {
        return help.toString();
    }

    @Override
    public Command newInstance() {
        CompareCommand cmd = new CompareCommand();
        cmd.setArguments(this.arguments);
        return cmd;
    }

    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);
    }
    
}
