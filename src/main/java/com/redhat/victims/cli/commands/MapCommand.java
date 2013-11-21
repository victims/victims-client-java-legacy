package com.redhat.victims.cli.commands;

import com.redhat.victims.cli.results.ExitInvalid;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitSuccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 *
 * @author gm
 */
public class MapCommand implements Command {

    private Usage help;
    private List<String> arguments; 
    private Map<String, Command> commands;
    private ExecutorCompletionService<CommandResult> completor;
    private List<Future<CommandResult>> results;

    public MapCommand(Map<String, Command> commands, 
            ExecutorCompletionService<CommandResult> completor, 
            List<Future<CommandResult>> results) {
        this.commands = commands;
        this.completor= completor;
        this.results = results;
        
        help = new Usage(getName(), "asynchronously maps a command to each input argument");
        help.addExample("scan file1.jar file2.jar file3.jar file4.jar");
        
    }
    
    @Override
    public CommandResult execute(List<String> args){
        
        if (args  == null || args.size() <= 1){
            return new ExitInvalid("invalid arguments");
        }
        
        String key = args.remove(0);
        Command cmd = commands.get(key);
        if (cmd == null){
            return new ExitInvalid(String.format("invalid command: %s", cmd));
        }
        
        for (String arg : args){
            ArrayList<String> params = new ArrayList();
            params.add(arg);
            Command tmp = cmd.newInstance();
            tmp.setArguments(params);
            results.add(completor.submit(tmp));
            
        }
        ExitSuccess rc = new ExitSuccess(null);
        rc.addVerboseOutput(String.format("Submitted %d %s tasks", args.size(), cmd.getName()));
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
        return "map";
    }

    @Override
    public String usage() {
        return help.toString();
    }
    
    @Override
    public Command newInstance(){
        return new MapCommand(this.commands, this.completor, this.results); 
    }
    
}

