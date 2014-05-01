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


import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsResultCache;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;

import java.util.logging.Logger;

/**
 * @author gm
 */
public class Environment {

    private static Environment self = new Environment();

    public static Environment getInstance() {
        return self;
    }

    private VictimsDBInterface database;
    private VictimsResultCache cache;
    private Logger log;

    private Environment() {}

    public VictimsDBInterface getDatabase() throws VictimsException {
        if (database == null){
            database = VictimsDB.db();
        }
        return database;
    }

    public VictimsResultCache getCache() throws VictimsException {
        if (cache == null){
            cache = new VictimsResultCache();
        }
        return cache;
    }

    public Logger getLog() {
        if (log == null){
            log = Logger.getLogger("com.redhat.victims.cli");
        }
        return log;
    }

}
