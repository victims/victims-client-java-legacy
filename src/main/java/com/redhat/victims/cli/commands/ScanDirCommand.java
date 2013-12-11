
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
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitSuccess;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.DirectoryWalker;

/**
 *
 * @author gm
 */
public class ScanDirCommand implements Command  {

    public static final String COMMAND_NAME = "scan-dir";
    private Usage help;
    private List<String> arguments;
    private boolean recursiveMode;
    protected Repl repl;
            
    class DirectoryScanner extends DirectoryWalker {
        
        private Repl repl; 
        private boolean recursive; 
        
        public DirectoryScanner(boolean recursive, Repl repl){
            super();
            this.repl = repl;
            this.recursive = recursive;
        }

        @Override
        protected boolean handleDirectory(File directory, int depth, Collection results) {
            if (!recursive && depth > 0) {
                return false; // break;
            }
            return true;
        }

        @Override
        protected void handleFile(File file, int depth, Collection results) {
            try {
                String commandName = null;
                ArrayList<String> args = new ArrayList();
                args.add(file.getCanonicalPath());
                
                if (file.getCanonicalPath().endsWith("pom.xml")) {
                    commandName = PomScannerCommand.COMMAND_NAME;
                } else {
                    commandName = ScanFileCommand.COMMAND_NAME;
                }
                
                Command command = repl.getCommand(commandName);
                if (command != null){
                    command.setArguments(args);
                    repl.scheduleExecution(command);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void scan(String filename) throws IOException {
            walk(new File(filename), null);
        }

    }
    
    public ScanDirCommand(boolean recursive, Repl dispatch){
        help = new Usage(getName(), "Scans the supplied directory for .jar files and reports any vulnerabilities found");
        help.addExample("/usr/share/java");
        recursiveMode = recursive;
        repl = dispatch;

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
        DirectoryScanner scanner = new DirectoryScanner(recursiveMode, repl);
        for (String arg : args){
            try{ 
                // dispatched and reported asynchronously
                scanner.scan(arg);
            } catch(IOException e){
                result.addVerboseOutput(String.format("error: (%s) - %s", arg, e.toString()));
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
        return new ScanDirCommand(this.recursiveMode, this.repl);
    }

    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);
    }
    
}
