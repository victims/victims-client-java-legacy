package com.redhat.victims.cli.commands;

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.util.List;

/**
 * @author gm
 */
public class LastUpdateCommand  implements Command {

    Usage help;
    public LastUpdateCommand(){
        help = new Usage(getName(), "returns the last time the database was updated");
        help.addExample("");
    }
  
    @Override
    public final String getName() {
        return "last-update";
    }

    @Override
    public CommandResult execute(List<String> args) {
          try { 
            VictimsDBInterface db = VictimsDB.db();       
            return new ExitSuccess(db.lastUpdated().toString());
                    
        } catch (VictimsException e){
            return new ExitFailure(e.getMessage());
        }
    }

    @Override
    public String usage() {
        return help.toString();
    }
    
}
