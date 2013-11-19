/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.victims.cli.commands;


import java.util.List;
import java.util.Map;

/**
 *
 * @author gm
 */
public class HelpCommand implements Command {

  private final Map<String, Command> commands;
  private Usage help;

  public HelpCommand(final Map<String, Command> commands) {
    this.commands = commands;
    help = new Usage(getName(), "displays help for each command");
    help.addExample("");
    help.addExample("config");
    
  }

  @Override
  public final String getName() {
    return "help";
  }

  @Override
  public CommandResult execute(List<String> args) {

    if (args == null || args.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String cmd : commands.keySet()) {
        sb.append(commands.get(cmd).usage());
        sb.append("\n");
      }
      return new ExitSuccess(sb.toString());
    }
    Command c = commands.get(args.get(0));
    if (c != null) {
      return new ExitSuccess(c.usage());
    }
    return new ExitFailure("unknown command: " + args.get(0));
  }

  @Override
  public String usage() {
    return help.toString();
  }
}