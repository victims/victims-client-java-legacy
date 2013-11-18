package com.redhat.victims.cli;


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

/**
 *
 * @author gm
 */
public class Repl {
    

    private String prompt; 
    private BufferedReader in;
    private PrintStream out;
    private Map<String, Command> commands;
    
    Repl(InputStream input, PrintStream output, String prompt){
        this.in = new BufferedReader(new InputStreamReader(input));
        this.out = output;
        this.prompt = prompt;
        this.commands = new HashMap();  
        register(new Help());
        register(new Exit());
    }
    
    Repl(){
        this(System.in, System.out, "victims >");
    }
    

    final void register(Command cmd){  
        commands.put(cmd.getName(), cmd);
    }
      
    final private static List<String> parse(String cmd) {
        
        List<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
        Matcher regexMatcher = regex.matcher(cmd);
        while (regexMatcher.find()) {
            matchList.add(TUI.unquote(regexMatcher.group()));
        }
        return matchList;
    }
    
    final String read(){
        try {
            out.printf("%s ", prompt);
            return in.readLine();
        } catch (IOException e){
        }
        
        return null;
    }
    
    final String eval(String cmd){
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
            
            String result = eval(input);
            if (result != null){
                out.println(result);
            }
            
        } 
    }
    
    // Built in commands 
    
    private class Help implements Command {
    
        public String getName(){
            return "help";
        }
        
        public String execute(List<String> args){
            
            if (args == null || args.isEmpty()){
                StringBuilder sb = new StringBuilder();
                for (String cmd : commands.keySet()){
                    sb.append(commands.get(cmd).usage());
                    sb.append("\n");
                }
                return sb.toString();
            }
            Command c = commands.get(args.get(0));
            if (c != null){
                return c.usage();
            }
            return "unknown command: " + args.get(0);
        }
        
        public String usage(){
            return TUI.formatUsage(getName(), 
                    "displays help for each command", 
                    "[<command>]");      
        }

    }
    
    private class Exit implements Command {
        
        public String getName(){
            return "exit";
        }
        
        public String execute(List<String> args){
            System.exit(0);
            return null;
        }
        
        public String usage(){
            return TUI.formatUsage(getName(), "quit this program", "");
        }
        
    }
    
}
