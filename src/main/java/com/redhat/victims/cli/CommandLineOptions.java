
package com.redhat.victims.cli;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gm
 */

class CommandLineOption<T> {
   
    final private String name;
    final private T defaultValue;
    final private String description;
    final private boolean required;
    final private boolean argumentExpected;
    final private boolean flag;
    final private Class type;
    private T value;
    private boolean valueSet;

    CommandLineOption(String name, String description, 
            boolean required, 
            boolean expectsArgument,
            boolean isFlag, 
            T defaultValue, 
            Class type){
        
        this.name = name;
        this.value = defaultValue;          // set default 
        this.defaultValue = defaultValue;   // save default
        this.description = description;
        this.required = required;
        this.argumentExpected = expectsArgument;
        this.flag = isFlag;
        this.valueSet = false;
        this.type = type;
    }
    
    String getName(){
        return name;
    }
    
    T getValue(){
        return value;
    }
    
    boolean flagSet(){
        return this.flag && this.valueSet;
    }
    
    Class getValueType(){
        return this.type;
    }
    
    void setFlag(){
        if (this.flag)
            this.valueSet = true;
    }   
    
    void setValue(T value){
       this.valueSet = true;
       this.value = value;
    }
    
    String getDescription(){
        
        int wrapAt = 40;
        if (description.length() > wrapAt){
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("  %-24s", name));
            for (int i = 0; i < description.length(); i+= wrapAt){
                if (i != 0){
                    sb.append(String.format("  %-24s", ""));
                }
                int diff = 0;
                int end = Math.min(i+wrapAt, description.length());
                if (end != description.length()){
                    
                    while(end > i && description.charAt(end) != ' '){
                        end--;
                        diff++;
                    }
                    
                    // don't break words
                    if (end == i)
                        continue;  
                }
                
                System.out.println("Substring = " + description.substring(i, end));
                sb.append(description.substring(i, end).trim());
                sb.append(String.format("%n"));
                
                i -= diff;
            }
            String wrapped = sb.toString();
            return wrapped.substring(0, wrapped.length()-1);
        }
        return String.format("  %-24s%s", name, description);
    }
    
    boolean isRequired(){
        return required;
    }
    
    boolean isFlag(){
        return flag;
    }
    
    boolean hasValue(){
        return this.value != null || this.valueSet ;
    }
    
    boolean expectsArgument(){
        return argumentExpected;
    }
    
    void reset(){
        this.valueSet = false;
        this.value = this.defaultValue;
    }
    
}

public class CommandLineOptions {

    Map<String, CommandLineOption> options;
    private String programName;
    private String detailedDescription;
    private List<String> errors;
    private List<String> arguments;
    private boolean haveRequiredArguments;

    public CommandLineOptions(String program){
        options = new HashMap();
        programName = program;
        errors = new ArrayList(); 
        arguments = new ArrayList();
        haveRequiredArguments = false;
        detailedDescription = null;
    }
       
    void addOption(CommandLineOption option){
        options.put(option.getName(), option);
        if (option.isRequired())
            haveRequiredArguments = true;
    }
    
    void setDescription(String description){
        detailedDescription = description;
    }
    
    Map<String, CommandLineOption> getOptions(){
        return this.options;
    }
    
    CommandLineOption getOption(String name){
        return this.options.get(name);
    }
    
    List<String> getArguments(){
        return this.arguments;
    }
    
    void reset(){
        errors.removeAll(errors);
        for (CommandLineOption opt : options.values()){
            opt.reset();
        }
    }
    
    boolean parse(String[] argv){
    
        int argc = argv.length;
        int argp = 0; 
          
        reset();
       
        // no arguments supplied
        if (argc == 0){
            for (CommandLineOption opt : options.values()){
                if (opt.isRequired() && ! opt.hasValue()){
                    errors.add(String.format("%s expected", opt.getName()));
                    return false;
                }
            }
            return true;
        }
        
        // parse options
        while (argp < argc && argc != 0){
            
            if (argv[argp].startsWith("-")){
                
                String argument = argv[argp];
                
                String value = null;
                if (argument.contains("=")){
                    String[] split = argument.split("=");
                    argument = split[0];
                    value = split[1];
                }
                
                CommandLineOption opt = options.get(argument);
                if (opt == null){
                    errors.add(String.format("Invalid option: %s", argv[argp]));
                    return false;
                }
                
                
                if (opt.isFlag()){
                    opt.setFlag();
                    
                } else  if (opt.expectsArgument() && argp+1 >= argc && value == null){
                    errors.add(String.format("%s requires an argument", opt.getName()));
                    return false;
                    
                } else {
                    argp++;
                    
                    if (value == null){
                        value = argv[argp];
                    }
                    
                    Class cls = opt.getValueType();
                    try { 
                        if (cls == Boolean.class)
                            opt.setValue(Boolean.parseBoolean(value));
                        else if (cls == Integer.class)
                            opt.setValue(Integer.parseInt(value));
                        else 
                            opt.setValue(value);
                        
                    } catch (NumberFormatException e){
                        errors.add(String.format("%s number expected: %s", opt.getName(), value));
                        return false;
                    }
                }
                argp++;
                
            } else {
                if (haveRequiredArguments){
                    errors.add(String.format("invalid option: %s", argv[argp]));
                    return false;
                } else {
                    arguments.add(argv[argp]);
                    argp++;
                }
            }
        }
        
        // ensure required fields have been set
        for (CommandLineOption opt : options.values()){
            if (opt.isRequired() && ! opt.hasValue()){
                return false;
            }
        }
          
        return true;
        
    }
    
    String getUsage(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("USAGE: ").append(programName).append(String.format(" [OPTIONS]...[ARGUMENTS]%n"));
        if (detailedDescription != null){
            sb.append(String.format("%n"));
            sb.append(detailedDescription);
            sb.append(String.format("%n"));
        }
        
        sb.append("OPTIONS:");
        sb.append(String.format("%n%n"));
        for (CommandLineOption option : options.values()){
            sb.append(option.getDescription()).append(String.format("%n%n"));         
        }
        
        for (String error : errors){
            sb.append(error).append(String.format("%n"));
        }
        
        return sb.toString();
    }
}
