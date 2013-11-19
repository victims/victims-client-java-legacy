
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
