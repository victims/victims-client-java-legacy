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
import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsResultCache;
import com.redhat.victims.VictimsScanner;
import com.redhat.victims.cli.Environment;
import com.redhat.victims.cli.results.CommandResult;
import com.redhat.victims.cli.results.ExitFailure;
import com.redhat.victims.cli.results.ExitInvalid;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author gm
 */
public class ScanFileCommand implements Command {

    public static final String COMMAND_NAME = "scan-file";

    private Usage help;
    private List<String> arguments;

    public ScanFileCommand(){
        help = new Usage(getName(), "Scans the supplied .jar file and reports any vulnerabilities");
        help.addExample("path/to/file.jar");
    }

    @Override
    public final String getName() {
        return COMMAND_NAME;
    }

    public String checksum(String filename) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            InputStream is = new FileInputStream(new File(filename));
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer)) > 0) {
                md.update(buffer, 0, count);
            }

            byte[] digest = md.digest();
            hash = String.format("%0" + (digest.length << 1) + "X", new BigInteger(1, digest));

        } catch (NoSuchAlgorithmException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return hash;

    }

    private boolean isEmptyFile(String filename){
        File check = new File(filename);
        return check.exists() && check.isFile() && check.length() == 0;
    }

    private Set<String> cachedVulnerabilities(String key) throws VictimsException {
        if (key != null && Environment.getInstance().getCache().exists(key)) {
            return Environment.getInstance().getCache().get(key);
        }
        return null;
    }

    private void cacheVulnerabilities(String key, Set<String> vulns) throws VictimsException {
        Environment.getInstance().getCache().add(key, vulns);
    }

    private void processRecords(ArrayList<VictimsRecord> records, CopyOnWriteArraySet<String> vulns, CommandResult result) throws VictimsException {

        // Reasonable number of records to process sequentially
        int cores = Runtime.getRuntime().availableProcessors();
        if (records.size() < cores * 2){
            for (VictimsRecord record : records){
                vulns.addAll(Environment.getInstance().getDatabase().getVulnerabilities(record));
            }

        // Otherwise process using worker pool
        } else {
            GetVulnerabilities[] workers = new GetVulnerabilities[cores];
            ConcurrentLinkedQueue<VictimsRecord> work = new ConcurrentLinkedQueue<VictimsRecord>(records);

            // Process work
            for (int i = 0; i < workers.length; ++i) {
                workers[i] = new GetVulnerabilities(work, vulns);
                workers[i].start();
            }

            /// Wait for jobs to be done
            for (int i = 0; i < workers.length; ++i) {

                try {
                    workers[i].join();
                    Throwable e = workers[i].getError();
                    if (e != null) {
                        result.addOutput("ERROR: " + e.getMessage());
                        result.addVerboseOutput(e.toString());
                    }

                } catch (InterruptedException e) {
                    throw new VictimsException("ERROR: " + e.getMessage());
                }
            }
        }
    }

    private void reportVulnerabilities(Collection<String> cves, String filename, CommandResult result){

        if (cves != null && cves.size() > 0){
            result.addOutput(String.format("%s VULNERABLE! ", filename));
            for (String cve : cves) {
                result.addOutput(cve);
                result.addOutput(" ");
            }

        } else {
            result.addVerboseOutput(filename + " - ok");
        }
    }

    @Override
    public CommandResult execute(List<String> args) {

        if (args == null) {
            return new ExitInvalid("file or directory expected");
        }

        CommandResult result = new CommandResult();
        for (String arg : args) {

            try {
                // Skip empty files..
                if (isEmptyFile(arg)){
                    result.addOutput(String.format("skipping %s (empty file)", arg));
                    continue;
                }

                // Use cache
                String key = checksum(arg);
                Set<String> cached = cachedVulnerabilities(key);
                if (cached != null) {
                    reportVulnerabilities(cached, arg, result);
                    continue;
                }

                // Perform Scan
                CopyOnWriteArraySet<String> scanResults = new CopyOnWriteArraySet<String>();
                processRecords(VictimsScanner.getRecords(arg), scanResults, result);
                reportVulnerabilities(scanResults, arg, result);

                // Cache results
                if (key != null){
                    cacheVulnerabilities(key, scanResults);
                }


            } catch (IOException e) {
                //e.printStackTrace();
                return new ExitFailure(e.getMessage());

            } catch (VictimsException e){
                //e.printStackTrace();
                return new ExitFailure(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public String usage() {
        return help.toString();
    }

    @Override
    public void setArguments(List<String> args) {
        this.arguments = args;
    }

    @Override
    public CommandResult call() throws Exception {
        return execute(this.arguments);
    }

    @Override
    public Command newInstance() {
        return new ScanFileCommand();
    }

    private class GetVulnerabilities extends Thread {


        private ConcurrentLinkedQueue<VictimsRecord> records;
        private CopyOnWriteArraySet<String> cves;
        private Throwable error;

        GetVulnerabilities(ConcurrentLinkedQueue<VictimsRecord> records,
                           CopyOnWriteArraySet<String> cves) {
            this.records = records;
            this.cves = cves;
        }

        public void run(){
            VictimsRecord record;

            while (error == null && (record = records.poll()) != null){
                try{
                    cves.addAll(Environment.getInstance().getDatabase().getVulnerabilities(record));
                } catch (VictimsException e){
                    error = e;
                    e.printStackTrace();
                }
            }
        }

        public Throwable getError(){
            return error;
        }
    }
}
