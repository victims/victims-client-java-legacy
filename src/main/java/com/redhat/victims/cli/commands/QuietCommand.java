
package com.redhat.victims.cli.commands;

import java.util.List;
import java.util.Map;

/**
 *
 * @author gm
 */
public final class QuietCommand implements Command {

  private Map<String, Boolean> flags; 
  private Usage help;
  public QuietCommand(Map<String, Boolean> flags){
      this.flags = flags;
      help = new Usage(getName(), "produces less verbose output");
      help.addExample("");
  }
  
  @Override
  public final String getName() {
       return "quiet";
  }

  @Override
  public CommandResult execute(List<String> args) {
    flags.put(getName(), Boolean.TRUE);
    return new ExitSuccess(null);
  }

  @Override
  public String usage() {
    return help.toString();
  }
  
}
