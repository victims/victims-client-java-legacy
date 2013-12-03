

package com.redhat.victims.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gm
 */
public class CommandLineOptionsTest {
    
    public CommandLineOptionsTest() {
    }

    @Test
    public void testAddOption() {
    }

    @Test
    public void testParse() {
        
//            CommandLineOption(String name, 
//                String description, 
//            boolean required, 
//            boolean expectsArgument,
//            boolean isFlag, 
//            T defaultValue){
    
        CommandLineOptions opts = new CommandLineOptions("java -jar victims-client.jar");
        opts.addOption(
            new CommandLineOption<Boolean>("-debug", "show debug output", false, false, true, null, Boolean.class));
        opts.addOption(
            new CommandLineOption<Boolean>("-help", "show this help message", false, false, true, null, Boolean.class));
        opts.addOption(
            new CommandLineOption<Integer>("-count", "example integer", false, true, false, null, Integer.class));
        opts.addOption(
            new CommandLineOption<String>("-key", "example kv entry", false, true, false, null, String.class)
        );
        
        // Empty arguments
        String[] emptyArguments = {};
        assert(opts.parse(emptyArguments));
        
        // Test flags
        String[] helpArgument = {"-help"};
        assertTrue(opts.parse(helpArgument));
        CommandLineOption opt = opts.getOptions().get("-help");
        opt.hasValue();
        assertTrue(opts.getOptions().get("-help").hasValue());
        opts.reset();
        assertTrue(! opts.getOptions().get("-help").hasValue());
        
        // Test invalid arguments
        String[] invalidArgument  = { "-count", "foo" };
        assertTrue(! opts.parse(invalidArgument));
        System.out.println(opts.getUsage());
        opts.reset();
        
        // Test key -> value entry
        String[] keyValueArgument = { "-key=value" };
        assertTrue(opts.parse(keyValueArgument));
        assertTrue(opts.getOptions().get("-key").getValue().equals("value"));
        
   
        // Test missing required 
        opts.addOption(
                new CommandLineOption<String>("-foo", "example foo", true, true, false, null, String.class ));
        assertTrue(! opts.parse(emptyArguments));
        System.out.println(opts.getUsage());
        
        String[] requiredArgument = { "-foo", "bar" };
        opts.reset();
        
        assertTrue(opts.parse(requiredArgument));
        
    }

    @Test
    public void testGetUsage() {
    }
    
}
