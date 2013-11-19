
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

/**
 *
 * @author gm
 */
public class CommandResult {
    private int rc; 
    private String output;
    
    public CommandResult(int returnCode, String stdout){
      output = stdout;
      rc = returnCode;
    }
    
    public boolean failed(){
        return rc != 0;
    }
    
    public int getResultCode(){
        return rc;
    }
    
    public String getOutput(){
        return output;
    }
    
    @Override 
    public String toString(){
        return output;
    }
    
}
