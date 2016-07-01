package org.carlspring.strongbox.data.tx;

import com.orientechnologies.orient.core.db.ODatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.orient.commons.core.OrientDatabaseFactory;
import org.springframework.data.orient.commons.core.OrientTransaction;
import org.springframework.data.orient.commons.core.OrientTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Alex Oreshkevich
 */
public class CustomOrientTransactionManager
        extends OrientTransactionManager
{

    private static final Logger logger = LoggerFactory.getLogger(CustomOrientTransactionManager.class);

    /**
     * Instantiates a new {@link OrientTransactionManager}.
     *
     * @param dbf the dbf
     */
    public CustomOrientTransactionManager(OrientDatabaseFactory<?> dbf)
    {
        super(dbf);
    }

    @Override
    protected synchronized void doBegin(Object transaction,
                                        TransactionDefinition definition)
            throws TransactionException
    {
        OrientTransaction tx = (OrientTransaction) transaction;

        ODatabase<?> db = tx.getDatabase();
        if (db == null || db.isClosed())
        {
            db = getDatabaseFactory().openDatabase();
            db.activateOnCurrentThread();
            tx.setDatabase(db);
            TransactionSynchronizationManager.bindResource(getDatabaseFactory(), db);
        }

        logger.trace("beginning transaction, db.hashCode() = {}", db.hashCode() + " URL: " + db.getURL());

        db.activateOnCurrentThread();
        if (db.isClosed())
        {
            db.open(getDatabaseFactory().getUsername(), getDatabaseFactory().getPassword());
        }

        db.begin();
    }

    @Override
    protected synchronized void doCommit(DefaultTransactionStatus status)
            throws TransactionException
    {
        OrientTransaction tx = (OrientTransaction)status.getTransaction();
        ODatabase db = tx.getDatabase();
        db.activateOnCurrentThread();
        db.commit();
    }
}
