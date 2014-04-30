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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author gm
 */
public class ScanFileCommand implements Command {

    public static final String COMMAND_NAME = "scan-file";

    private VictimsDBInterface db;
    private VictimsResultCache cache;
    private Usage help;
    private List<String> arguments;

    public ScanFileCommand() {
        this(null, null);
    }

    public ScanFileCommand(VictimsDBInterface database, VictimsResultCache resultCache){
        help = new Usage(getName(), "Scans the supplied .jar file and reports any vulnerabilities");
        help.addExample("path/to/file.jar");
        db = database;
        cache = resultCache;
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

    private VictimsDBInterface getDatabase() throws VictimsException {
        if (db == null) {
            db = VictimsDB.db();
        }
        return db;
    }

    private VictimsResultCache getCache() throws VictimsException {
        if (cache == null){
            cache = new VictimsResultCache();
        }
        return cache;
    }

    @Override
    public CommandResult execute(List<String> args) {

        if (args == null) {
            return new ExitInvalid("file or directory expected");
        }

        try {
            getDatabase();
            getCache();

        } catch (VictimsException e) {
            //e.printStackTrace();
            return new ExitFailure(e.getMessage());
        }

        CommandResult result = new CommandResult();
        for (String arg : args) {

            // Skip empty files.. 
            File check = new File(arg);
            if (check.exists() && check.isFile() && check.length() == 0){
                result.addOutput(String.format("skipping %s (empty file)", arg));
                continue;
            } 
            
            // Check cache 
            String key = checksum(arg);
            if (key != null && cache.exists(key)) {
                try {
                    HashSet<String> cves = cache.get(key);
                    if (cves != null && cves.size() > 0) {
                        result.addOutput(String.format("%s VULNERABLE! ", arg));
                        for (String cve : cves) {
                            result.addOutput(cve);
                            result.addOutput(" ");
                        }

                        continue;
                    } else {
                        result.addVerboseOutput(arg + " ok");
                    }
                } catch (VictimsException e) {
                    //e.printStackTrace();
                    result.addVerboseOutput(e.getMessage());
                }
            }

            // Scan the item
            ArrayList<VictimsRecord> records = new ArrayList();
            try {

                VictimsScanner.scan(arg, records);
                for (VictimsRecord record : records) {

                    try {
                        HashSet<String> cves = db.getVulnerabilities(record);
                        if (key != null) {
                            cache.add(key, cves);
                        }
                        if (!cves.isEmpty()) {
                            result.addOutput(String.format("%s VULNERABLE! ", arg));
                            for (String cve : cves) {
                                result.addOutput(cve);
                                result.addOutput(" ");
                            }
                        } else {
                            result.addVerboseOutput(arg + " ok");
                        }

                    } catch (VictimsException e) {
                        //e.printStackTrace();
                        return new ExitFailure(e.getMessage());
                    }
                }
            } catch (IOException e) {
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
        return new ScanFileCommand(db, cache);
    }

}
