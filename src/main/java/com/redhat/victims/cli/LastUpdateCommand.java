package com.redhat.victims.cli;

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.util.List;

/**
 *
 * @author gm
 */
public class LastUpdateCommand  implements Command {

    @Override
    public String getName() {
        return "last-update";
    }

    @Override
    public String execute(List<String> args) {
          try { 
            VictimsDBInterface db = VictimsDB.db();         
            return db.lastUpdated().toString();
                    
        } catch (VictimsException e){
            return String.format("error: %s", e.getMessage());
        }
    }

    @Override
    public String usage() {
        return TUI.formatUsage(getName(), "returns the last time the database was updated", "");
    }
    
}
