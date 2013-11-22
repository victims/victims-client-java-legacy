package com.redhat.victims.cli;

import com.redhat.victims.cli.commands.Command;
import com.redhat.victims.cli.commands.MapCommand;
import com.redhat.victims.cli.commands.ScanCommand;
import com.redhat.victims.cli.results.CommandResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gm
 */
public class ReplTest {

    static String testDataDir = ".testdata";
    static String[] jars = {
        "/org/wildfly/wildfly-jaxrs/8.0.0.Beta1/wildfly-jaxrs-8.0.0.Beta1.jar",
        "/org/springframework/spring/2.5.6/spring-2.5.6.jar",
        "/org/wildfly/wildfly-jaxrs/8.0.0.Alpha1/wildfly-jaxrs-8.0.0.Alpha1.jar",
        "/org/wildfly/wildfly-jaxrs/8.0.0.Alpha1/wildfly-jaxrs-8.0.0.Alpha1-sources.jar",
        "/org/wildfly/wildfly-cli/8.0.0.Beta1/wildfly-cli-8.0.0.Beta1-client.jar",
        "/org/wildfly/wildfly-osgi-http/8.0.0.Alpha1/wildfly-osgi-http-8.0.0.Alpha1.jar",
        "/org/jboss/web/jbossweb/7.0.17.Final/jbossweb-7.0.17.Final.jar",
        "/org/wildfly/wildfly-osgi-http/8.0.0.Alpha1/wildfly-osgi-http-8.0.0.Alpha1-sources.jar"
    };

    static String getVulnerableFile(String filename) throws UnsupportedEncodingException, IOException {

        File testFile = new File(testDataDir, filename);
        if (testFile.exists()) {
            return testFile.getAbsolutePath();
        }
        testFile.getParentFile().mkdirs();
        testFile.createNewFile();
        String charset = "UTF-8";
        String url = String.format("http://search.maven.org/remotecontent?filepath=%s",
                URLEncoder.encode(filename, charset));
        URLConnection client = new URL(url).openConnection();
        InputStream response = client.getInputStream();
        FileOutputStream out = new FileOutputStream(testFile.getAbsolutePath());

        byte[] buffer = new byte[4096];
        int nread;
        while ((nread = response.read(buffer)) > 0) {
            out.write(buffer, 0, nread);
        }

        response.close();
        out.close();

        return testFile.getAbsolutePath();
    }

    public ReplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        
        System.setProperty(Repl.VERBOSE, "true");
        for (String file : jars) {
            try {
                getVulnerableFile(file); // just prefill the .testdata dir

            } catch (Exception e) {
                e.printStackTrace();
                System.err.printf("too bad: %s%n", e.getMessage());
            }
        }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testShutdown() throws Exception {
    }

    @Test
    public void testRead() {
    }

    @Test
    public void testEval() {
    }

    @Test
    public void testPrint() {
    }

    @Test
    public void testLoop() throws UnsupportedEncodingException {
        StringBuilder inputData = new StringBuilder();
        for (String file : jars) {
            String cmd = String.format(String.format("scan %s%s%n", testDataDir, file));
            inputData.append(cmd);
        }

        InputStream in = new ByteArrayInputStream(inputData.toString().getBytes("UTF-8"));   
        System.out.println("Begin scan test");
        System.setProperty(Repl.VERBOSE, "true");
        Repl repl = new Repl(in, System.out, "victims~> ");
        repl.register(new ScanCommand());

        repl.loop();
        System.out.println("End scan test");
        
    }
    
    @Test
    public void testMap() throws UnsupportedEncodingException {
        StringBuilder inputData = new StringBuilder();
        inputData.append("map scan ");
        for (String file : jars) {
            inputData.append(testDataDir);
            inputData.append(file);
            inputData.append(" ");
        }
        inputData.append(String.format("%n"));

        InputStream in = new ByteArrayInputStream(inputData.toString().getBytes("UTF-8"));   

        System.out.println(inputData.toString());
        System.out.println("Begin map test");
        Repl repl = new Repl(in, System.out, "victims~>");
        repl.register(new ScanCommand());        
        repl.loop();
     
        System.out.println("End map test");
        
    }

}
