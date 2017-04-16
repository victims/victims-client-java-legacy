
package com.redhat.victims.cli.results;

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
    
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_INVALID = 1<<1;
    public static final int RESULT_ERROR   = 1<<2;
    public static final int RESULT_UNKNOWN = -1;
    
    private int rc; 
    private final StringBuilder output;
    private final StringBuilder verbose;
    
    public CommandResult(){
        rc = RESULT_UNKNOWN;
        output = new StringBuilder();
        verbose = new StringBuilder();   
    }
    public CommandResult(int returnCode, String stdout){
      this();
      if (stdout != null)
        addOutput(stdout);
      rc = returnCode;
    }
    
    public boolean success(){
        return rc == RESULT_SUCCESS;
    }
    
    public boolean invalidUsage(){
        return rc == RESULT_INVALID;
    }
    
    public boolean failed(){
        return rc == RESULT_ERROR;
    }
    
    public boolean unkown(){
        return rc == RESULT_UNKNOWN;
    }
    
    public int getResultCode(){
        return rc;
    }
        
    public void setResultCode(int resultCode){
        this.rc = resultCode;
    }
    
    public String getOutput(){
        return output.toString();
    }
    
    public String getVerboseOutput(){
        return verbose.toString();
    }
    
    public void addOutput(String data){
        output.append(data);
    }
    public void addVerboseOutput(String data){
        verbose.append(data);
    }
    
    @Override 
    public String toString(){
        return output.toString();
    }
    
}
