package com.redhat.victims.cli;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.redhat.victims.cli.commands.Command;
import com.redhat.victims.cli.commands.CommandResult;
import com.redhat.victims.cli.commands.HelpCommand;
import com.redhat.victims.cli.commands.ExitCommand;
import com.redhat.victims.cli.commands.QuietCommand;

/**
 *
 * @author gm
 */
public class Repl {
    
   
    private String prompt; 
    private BufferedReader in;
    private PrintStream out;
    private Map<String, Command> commands;
    private Map<String, Boolean> flags;
    
    Repl(InputStream input, PrintStream output, String prompt){
        this.in = new BufferedReader(new InputStreamReader(input));
        this.out = output;
        this.prompt = prompt;
        this.commands = new HashMap();  
        this.flags = new HashMap();
        register(new HelpCommand(this.commands));
        register(new QuietCommand(this.flags));
        register(new ExitCommand());
    }
    
    Repl(){
        this(System.in, System.out, ">");
    }
    

    final void register(Command cmd){  
        commands.put(cmd.getName(), cmd);
    }
      
    final private static String unquote(String s){
        if (s.isEmpty()){
            return s;
        }
        int start = 0; 
        int end = s.length();
        if (s.startsWith("\'") || s.startsWith("\"")){
            start++;
        }
        if (s.endsWith("\'") || s.endsWith("\"")){
            end--;
        }
     
        return s.substring(start, end);
       
    }
    
    final private static List<String> parse(String cmd) {
        
        List<String> tokens = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
        Matcher regexMatcher = regex.matcher(cmd);
        while (regexMatcher.find()) {
            tokens.add(unquote(regexMatcher.group()));
        }
        return tokens;
    }
    
    final String read(){
        try {
            Boolean quietMode = flags.get("quiet");
            if (quietMode == null || ! quietMode.booleanValue())
              out.printf("%s ", prompt);
            
            return in.readLine();
        } catch (IOException e){
        }
        
        return null;
    }
    
    final CommandResult eval(String cmd){
        if (cmd == null)
            return null;
        
        List<String> tokens = parse(cmd);
       
        if (tokens.isEmpty()){
            return null;
        }
        
        String commandName = tokens.remove(0);
        Command c = commands.get(commandName);
        
        if (c == null){
            out.printf("invalid command: %s%n", commandName);
            return null;
        }
        
        if (tokens.isEmpty()){
            return c.execute(null);
        } 
        return c.execute(tokens);
        
    }
    
    final void loop(){
        
        while(true){
            String input = read();
            if (input == null){
                out.println();
                break; // eof
            }
            
            CommandResult result = eval(input);
            if (result != null && result.getOutput() != null){
                out.println(result);
                if (result.failed()){
                    System.exit(result.getResultCode());
                }
            }    
        } 
    }
  
}
