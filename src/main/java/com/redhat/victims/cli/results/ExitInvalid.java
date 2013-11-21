
package com.redhat.victims.cli.results;

/**
 *
 * @author gm
 */
public class ExitInvalid extends CommandResult {
    
    public ExitInvalid(String reason){
        super(CommandResult.RESULT_INVALID, reason);
    }
    
}
