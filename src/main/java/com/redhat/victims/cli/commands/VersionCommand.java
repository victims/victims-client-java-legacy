package com.redhat.victims.cli.commands;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitFailure;
import com.redhat.victims.cli.results.ExitSuccess;

public class VersionCommand implements Command {

    public static final String COMMAND_NAME = "version";
    private Usage help;
    private List<String> arguments;

        
    public VersionCommand(){
        help = new Usage(getName(), "Display the client version");
        help.addExample("");
    }
    
    
    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);

    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public void setArguments(List<String> args) {
        this.arguments = args;

    }

    @Override
    public CommandResult execute(List<String> args) {
        
        URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
        try {
          URL url = cl.findResource("META-INF/MANIFEST.MF");
          Manifest manifest = new Manifest(url.openStream());
          ExitSuccess result = new ExitSuccess(null);
          result.addOutput("Victims Client for Java - ");
          result.addOutput(manifest.getMainAttributes().getValue("Client-Version"));
          result.addOutput(" (build: ");
          result.addOutput(manifest.getMainAttributes().getValue("Build-Time"));
          result.addOutput(")");
          
          return result;
        } catch (IOException e) {
            return new ExitFailure("Cannot access manifest: " + e.getMessage());
        }
    }

    @Override
    public String usage() {
        return help.toString();

    }

    @Override
    public Command newInstance() {
        return new VersionCommand();
    }

}
