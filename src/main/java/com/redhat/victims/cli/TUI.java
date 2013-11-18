package com.redhat.victims.cli;


/**
 *
 * @author gm
 */
public class TUI {
    
    public static final String SUCCESS = "ok";
    public static final String FAILURE = "error"; 
  
    
    final static String formatUsage(String cmd, String usage, String example){
        if (example != null && example.length() > 0)
            return String.format("%s - %s%n    %s %s%n", cmd, usage, cmd, example);
        
        return String.format("%s - %s%n", cmd, usage);
    }
    
    final static String formatUsage(String cmd, String usage, String[] uses){
        
        StringBuilder sb = new StringBuilder(); 
        sb.append(String.format("%s - %s%n", cmd, usage));
        for (String use : uses){
            sb.append(String.format("    %s %s %n", cmd, use));
        }
        return sb.toString();
    }
    
    final static String join(Iterable<String> items, String sep) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item);
            sb.append(sep);
        }
        String rv = sb.toString();
        return rv.substring(0, rv.length() - sep.length());
    }
    
     final public static String unquote(String s){
        if (s.isEmpty()){
            return s;
        }
        int start = 0; 
        int end = s.length();
        if (s.startsWith("\'") || s.startsWith("\"")){
            start++;
        }
        if (s.endsWith("\'") || s.endsWith("\"")){
            end--;
        }
     
        return s.substring(start, end);
       
    }
         
    
}
