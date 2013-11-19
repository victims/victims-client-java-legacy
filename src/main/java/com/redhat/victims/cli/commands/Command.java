package com.redhat.victims.cli.commands;


import java.util.List;

/**
 *
 * @author gm
 */
public interface Command {
    public String getName();
    public CommandResult execute(List<String> args);    
    public String usage();
}
