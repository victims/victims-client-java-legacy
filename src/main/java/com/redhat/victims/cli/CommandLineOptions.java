
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
import java.util.Arrays;
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
    final private boolean flag;
    final private Class<T> type;
    //private T value;
    private ArrayList<T> values;
    private int numberOfArgumentsExpected;
    private boolean valueSet;

    CommandLineOption(String name, String description, 
            boolean required, 
            int argumentsExpected,
            boolean isFlag, 
            T defaultValue, 
            Class<T> type){
        
        this.name = name;
        this.defaultValue = defaultValue;   // save default
        this.description = description;
        this.required = required;
        this.numberOfArgumentsExpected = argumentsExpected;
        this.flag = isFlag;
        this.valueSet = false;
        this.type = type;

        reset();
    }
    
    String getName(){
        return name;
    }
    
    T getValue(){
        return getValueAt(0);
    }

    T getValueAt(int index){
        if (this.values == null || this.values.isEmpty()){
            return null;
        }
        return this.values.get(index);
    }
    
    boolean flagSet(){
        return this.flag && this.valueSet;
    }
    
    Class<T> getValueType(){
        return this.type;
    }
    
    void setFlag(){
        if (this.flag)
            this.valueSet = true;
    }   
    
    void setValue(T value, T...values){
        this.valueSet = true;
        if (this.values == null){
            this.values = new ArrayList<T>();
        }
        this.values.add(value);
        for (T val : values){
            this.values.add(val);
        }
    }
    
    void addValue(T arg){
        this.valueSet = true;
        if (this.values == null){
            this.values = new ArrayList<T>();
        }
        this.values.add(arg);
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
        return (this.values != null
                && this.values.size() > 0 )
                || this.valueSet;
        //return this.value != null || this.valueSet ;
    }
    
    boolean expectsArgument(){
        return numberOfArgumentsExpected > 0;
        //return argumentExpected;
    }
    
    int expectedArgumentCount(){
        return numberOfArgumentsExpected;
    }
    
    void reset(){
        this.valueSet = false;
        this.values = new ArrayList<T>();
        if (this.defaultValue != null){
            this.valueSet = true;
            this.values.add(this.defaultValue);
        }
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
    private boolean addValueToOption(CommandLineOption opt, String val){

        Class cls = opt.getValueType();
        try { 
            if (cls == Boolean.class)
                opt.addValue(Boolean.parseBoolean(val));
            else if (cls == Integer.class)
                opt.addValue(Integer.parseInt(val));
            else 
                opt.addValue(val);
        }  catch (NumberFormatException e){
            errors.add(String.format("%s number expected: %s", opt.getName(), val));
            return false;
        }
        return true;    
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
                    errors.add(String.format("%s requires additional arguments", opt.getName()));
                    return false;
                
                } else if (value != null){
                    if (! addValueToOption(opt, value)){
                        return false;
                    }
                        
                } else {
                    argp++;
                    int processed = 0; 
                    while (processed < opt.expectedArgumentCount() && argp < argc){
                        String arg = argv[argp++];
                        if (arg.startsWith("-")){
                            break;
                        }
                        if (! addValueToOption(opt, arg)){
                            return false;
                        }
                        processed++;  
                    }
                    if (processed != opt.expectedArgumentCount()){
                        errors.add(String.format("%s expects %d arguments", opt.getName(), opt.expectedArgumentCount()));
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

        // Ensure options are printed in alphabetical order
        String[] keys = new String[options.keySet().size()];
        options.keySet().toArray(keys);
        Arrays.sort(keys);
        
        for (String k : keys){
            CommandLineOption option = options.get(k);
            sb.append(option.getDescription()).append(String.format("%n%n"));         
        }
        
        for (String error : errors){
            sb.append(error).append(String.format("%n"));
        }
        
        return sb.toString();
    }
}
