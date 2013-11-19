
package com.redhat.victims.cli.commands;

/**
 *
 * @author gm
 */
public class ExitFailure extends CommandResult {
    public ExitFailure(String error){
        super(-1, String.format("error: %s", error));
    }
  
}
