package com.redhat.victims.cli.commands;

import com.redhat.victims.cli.results.CommandResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gm
 */
public class ScanCommandTest {
    
    static String testDataDir = ".testdata";

    String createEmptyJar() throws Exception {
        
        File testFile = new File(testDataDir, "empty.jar");

        if (testFile.exists()) {
            return testFile.getAbsolutePath();
        }
        testFile.getParentFile().mkdirs();
        testFile.createNewFile();
        
        return testFile.getAbsolutePath();
        
    }
    
    @Test
    public void testScanEmptyFile() throws Exception {
    
        // Create an empty file. 
        String filename = createEmptyJar();
        
        // Purge the cache 
        System.setProperty("victims.cache.purge", "true");
          
        
        ScanFileCommand cmd = new ScanFileCommand();
        List<String> args = new ArrayList(); 
        args.add(filename);
        //args.add(".testdata/org/springframework/spring/2.5.6/spring-2.5.6.jar");
        
        CommandResult result = cmd.execute(args);
        System.out.println(result.getVerboseOutput());
        
       
    }

    


    
}
