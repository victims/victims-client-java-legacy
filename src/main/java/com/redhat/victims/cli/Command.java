package com.redhat.victims.cli;


import java.util.List;

/**
 *
 * @author gm
 */
interface Command {
    String getName();
    String execute(List<String> args);    
    String usage();
}
