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
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitTerminate;
import java.util.List;

/**
 *
 * @author gm
 */
public class ExitCommand implements Command {

    private Usage help;
    private List<String> arguments;

    public ExitCommand() {
        help = new Usage(getName(), "exit from interactive mode");
    }

    @Override
    public final String getName() {
        return "exit";
    }

    @Override
    public CommandResult execute(List<String> args) {
        return new ExitTerminate(CommandResult.RESULT_SUCCESS);
    }

    @Override
    public String usage() {
        return help.toString();
    }

    @Override
    public void setArguments(List<String> args) {
        this.arguments = args;
    }

    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);
    }
    
    @Override
    public Command newInstance(){
        return new ExitCommand();
    }

}
