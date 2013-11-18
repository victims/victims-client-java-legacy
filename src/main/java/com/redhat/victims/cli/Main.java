package com.redhat.victims.cli;


/**
 *
 * @author gm
 */
public class Main {
    public static void main(String args[]){
        
        Repl repl = new Repl();
        repl.register(new ConfigureCommand());
        repl.register(new LastUpdateCommand());
        repl.register(new SynchronizeCommand());
        repl.register(new ScanCommand());
        
        if (args.length > 0){
            StringBuilder sb = new StringBuilder();
            for (String arg : args){
                sb.append(arg);
                sb.append(" ");
            }
            System.out.println(repl.eval(sb.toString()));
   
        } else {
            repl.loop();
        }
        
    }
    
}
