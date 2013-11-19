
package com.redhat.victims.cli.commands;

/*
 * #%L
 * This file is part of victims-client.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


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