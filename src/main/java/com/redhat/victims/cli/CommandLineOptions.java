
package com.redhat.victims.cli;

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
    private T value;
    private T defaultValue;
    final private String description;
    final private boolean required;
    final private boolean argumentExpected;
    private boolean valueSet;
    private boolean flag;
    private Class type; 
    
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
        return String.format("    %s\t: %s", name, description);
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
    private List<String> errors;

    public CommandLineOptions(String program){
        options = new HashMap();
        programName = program;
        errors = new ArrayList(); 
    }
       
    void addOption(CommandLineOption option){
        options.put(option.getName(), option);
    }
    
    Map<String, CommandLineOption> getOptions(){
        return this.options;
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
                
                argp++;
                if (opt.isFlag()){
                    opt.setFlag();
                } else  if (opt.expectsArgument() && argp >= argc && value == null){
                    errors.add(String.format("%s requires an argument", opt.getName()));
                    return false;
                    
                } else {
                    
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
                errors.add(String.format("invalid option: %s", argv[argp]));
                return false;
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
        sb.append("USAGE: ").append(programName).append(String.format(" [OPTIONS]...%n"));
        
        for (CommandLineOption option : options.values()){
            sb.append(option.getDescription()).append(String.format("%n"));         
        }
        
        
        for (String error : errors){
            sb.append(error).append(String.format("%n"));
        }
        
        return sb.toString();
    }
}
