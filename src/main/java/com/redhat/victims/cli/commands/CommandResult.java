
package com.redhat.victims.cli.commands;

/**
 *
 * @author gm
 */
public class CommandResult {
    private int rc; 
    private String output;
    
    public CommandResult(int returnCode, String stdout){
      output = stdout;
      rc = returnCode;
    }
    
    public boolean failed(){
        return rc != 0;
    }
    
    public int getResultCode(){
        return rc;
    }
    
    public String getOutput(){
        return output;
    }
    
    @Override 
    public String toString(){
        return output;
    }
    
}
