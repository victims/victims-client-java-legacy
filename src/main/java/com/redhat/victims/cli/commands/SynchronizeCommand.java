package com.redhat.victims.cli.commands;

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.util.List;

/**
 * @author gm
 */
public class SynchronizeCommand implements Command {

    private Usage help;
    
    public SynchronizeCommand(){
      help = new Usage(getName(), "update the victims database definitions");
      help.addExample("");
    }
    
    @Override
    public final String getName() {
        return "sync";
    }

    @Override
    public CommandResult execute(List<String> args) {
        try { 
            VictimsDBInterface db = VictimsDB.db();
            db.synchronize();
            return new ExitSuccess("syncrhonization completed successfully");
                    
        } catch (VictimsException e){
            return new ExitFailure(e.getMessage());
        }
    }

    @Override
    public String usage() {
        return help.toString();
    }
    
}
