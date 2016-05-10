package org.carlspring.strongbox.db;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.util.function.Consumer;
import java.util.function.Function;

public class DbUtils {

    public static <R> R withDatabase(Function<OObjectDatabaseTx, R> code)
    {
        // for simplicity use inmemory database
        // TODO Replace it with remote instance!
        OObjectDatabaseTx db = new OObjectDatabaseTx("memory:strongbox");
        try
        {
            if (db.exists())
            {
                ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
                db.open("admin", "admin");
            }
            else
            {
                db.create();
            }
            return code.apply(db);
        }
        finally
        {
/*            if (!db.isClosed())
            {
                db.close();
            }*/
        }
    }

    public static <R> void withDatabase(Consumer<OObjectDatabaseTx> code)
    {
        // for simplicity use inmemory database
        // TODO Replace it with remote instance!
        OObjectDatabaseTx db = new OObjectDatabaseTx("memory:strongbox");
        try
        {
            if (db.exists())
            {
                ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
                db.open("admin", "admin");
            }
            else
            {
                db.create();
            }

            code.accept(db);
        }
        finally
        {
/*            if (!db.isClosed())
            {
                db.close();
            }*/
        }
    }

}
