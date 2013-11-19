
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gm
 */
public class Usage {
  
  private String command;
  private String description;
  private List<String> examples;
  
  public Usage(String cmd, String description){
    this.command = cmd;
    this.description = description;
    this.examples = new ArrayList(); 
  }
  
  public void addExample(String example){
    examples.add(example);
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    
    sb.append(String.format("%s - %s%n", command, description));
    for (String example : examples){
      sb.append(String.format("    %s %s%n", command, example));
    }
   
    return sb.toString();
  }
  
}
