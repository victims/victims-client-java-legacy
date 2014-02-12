package com.redhat.victims.cli;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.redhat.victims.cli.commands.Command;
import com.redhat.victims.cli.commands.CompareCommand;
import com.redhat.victims.cli.commands.ConfigureCommand;
import com.redhat.victims.cli.commands.DumpCommand;
import com.redhat.victims.cli.commands.LastUpdateCommand;
import com.redhat.victims.cli.commands.PomScannerCommand;
import com.redhat.victims.cli.commands.ScanDirCommand;
import com.redhat.victims.cli.commands.ScanFileCommand;
import com.redhat.victims.cli.commands.SynchronizeCommand;
import com.redhat.victims.cli.results.CommandResult;

class ExceptionThrowingCommand implements Command {
	
	public static String COMMAND_NAME = "throw-exception";
	
	private ArrayList<String> arguments;

	@Override
	public CommandResult call() throws Exception {
		throw new NullPointerException("Opps.. you broke it?");	
	}

	@Override
	public String getName() {
		return COMMAND_NAME; 
	}

	@Override
	public void setArguments(List<String> args) {
		if (args != null && args.size() > 0){
			arguments.addAll(args);
		}
		
	}

	@Override
	public CommandResult execute(List<String> args) {
		setArguments(args);
		try { 
			return this.call();
		} catch(Exception e){
			return null;
		}
		
	}

	@Override
	public String usage() {
		return "throw-exception"; 
	}

	@Override
	public com.redhat.victims.cli.commands.Command newInstance() {
		return new ExceptionThrowingCommand();
	}
	
}

public class RuntimeErrorTest extends Main   {

	@Test
	public void testRunWithArgs() throws Exception {
		
		//Mimic main program.
		System.setProperty(Repl.VERBOSE, "true");
		Repl repl = new Repl();
		repl.register(new ConfigureCommand());
		repl.register(new LastUpdateCommand());
		repl.register(new SynchronizeCommand());
		repl.register(new ScanFileCommand());
		repl.register(new ScanDirCommand(true, repl));
		repl.register(new PomScannerCommand());
		repl.register(new DumpCommand());
		repl.register(new CompareCommand());
		
		// Test unexpected command failure.. 
		repl.register(new ExceptionThrowingCommand());

		// Long running command..
		repl.runCommand(SynchronizeCommand.COMMAND_NAME);
		repl.runCommand(ExceptionThrowingCommand.COMMAND_NAME);
		
		for (CommandResult result : repl.completedCommands()){
			repl.print(result);
			if (result != null && result.failed()){
				break;
			} 
		}
		repl.shutdown();

	
	}

}
