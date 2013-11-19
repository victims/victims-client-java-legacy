/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.victims.cli.commands;

import java.util.List;

/**
 *
 * @author gm
 */
public class ExitCommand implements Command {

  private Usage help;
  
  public ExitCommand(){
    help = new Usage(getName(), "exit from interactive mode");
  }
  
  @Override
  public final String getName() {
    return "exit";
  }

  @Override
  public CommandResult execute(List<String> args) {
    System.exit(0);
    return null; // not reachable
  }

  @Override
  public String usage() {
    return help.toString();
  }
}