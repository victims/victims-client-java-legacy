
package com.redhat.victims.cli.commands;

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
