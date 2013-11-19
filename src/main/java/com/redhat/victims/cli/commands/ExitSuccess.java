
package com.redhat.victims.cli.commands;

/**
 *
 * @author gm
 */
public class ExitSuccess extends CommandResult {

    public ExitSuccess(String output){
      super(0, output);
    }
 
}
