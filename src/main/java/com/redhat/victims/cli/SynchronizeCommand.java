package com.redhat.victims.cli;

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.util.List;

/**
 * @author gm
 */
public class SynchronizeCommand implements Command {

    @Override
    public String getName() {
        return "sync";
    }

    @Override
    public String execute(List<String> args) {
        try { 
            VictimsDBInterface db = VictimsDB.db();
            db.synchronize();
            return TUI.SUCCESS;
                    
        } catch (VictimsException e){
            return String.format("error: %s", e.getMessage());
        }
        
    }

    @Override
    public String usage() {
        return TUI.formatUsage(getName(), "update the victims database definitions", "");
    }
    
}
