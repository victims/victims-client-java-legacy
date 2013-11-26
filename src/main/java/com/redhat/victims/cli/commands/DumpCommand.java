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

import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitSuccess;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gm
 */
public class DumpCommand implements Command {

        
    private Usage help;
    private List<String> arguments; 
    
    public DumpCommand(){
        help = new Usage(getName(), "Shows a fingerprint for a .jar file");
        help.addExample("example.jar");       
    }
    
    @Override
    public final String getName() {
        return "dump";
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
                ArrayList<VictimsRecord> records = new ArrayList();
                VictimsScanner.scan(arg, records);
                for (VictimsRecord record : records){
                    result.addOutput(record.toString());
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
        DumpCommand cmd = new DumpCommand();
        cmd.setArguments(this.arguments);
        return cmd;
    }

    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);
    }
    
}
