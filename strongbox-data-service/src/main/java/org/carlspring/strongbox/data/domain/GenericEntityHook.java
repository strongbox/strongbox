package org.carlspring.strongbox.data.domain;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.hook.ORecordHookAbstract;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * @author Sergey Bespalov
 *
 */
public class GenericEntityHook extends ORecordHookAbstract
{
    
    private static final Logger Logger = LoggerFactory.getLogger(GenericEntityHook.class);

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode()
    {
        return DISTRIBUTED_EXECUTION_MODE.BOTH;
    }

    @Override
    public RESULT onRecordBeforeCreate(ORecord iRecord)
    {
        if (!(iRecord instanceof ODocument))
        {
            return RESULT.RECORD_NOT_CHANGED;
        }
        ODocument doc = (ODocument) iRecord;

        String uuid = doc.field("uuid");
        if (uuid != null && !uuid.trim().isEmpty())
        {
            return RESULT.RECORD_NOT_CHANGED;
        }
        uuid = UUID.randomUUID().toString();
        Logger.debug(String.format("Found empty 'uuid', default unique value [%s] generated", uuid));
        doc.field("uuid", uuid);
        
        return RESULT.RECORD_CHANGED;
    }

}
