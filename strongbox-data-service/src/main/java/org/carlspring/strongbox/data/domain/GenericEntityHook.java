package org.carlspring.strongbox.data.domain;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.hook.ORecordHookAbstract;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * @author Sergey Bespalov
 *
 */
public class GenericEntityHook extends ORecordHookAbstract
{

    private static final Logger logger = LoggerFactory.getLogger(GenericEntityHook.class);

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode()
    {
        return DISTRIBUTED_EXECUTION_MODE.BOTH;
    }

    @Override
    public RESULT onRecordBeforeCreate(ORecord iRecord)
    {
        RESULT result = RESULT.RECORD_NOT_CHANGED;
        if (!(iRecord instanceof ODocument))
        {
            return result;
        }
        ODocument doc = (ODocument) iRecord;

        String uuid = doc.field("uuid");
        if (uuid == null || uuid.trim().isEmpty())
        {
            uuid = UUID.randomUUID().toString();
            logger.debug(String.format("Found empty 'uuid', default unique value generated [%s].", uuid));
            doc.field("uuid", uuid);

            result = RESULT.RECORD_CHANGED;
        }

        for (OClass oClass : doc.getSchemaClass().getAllSuperClasses())
        {
            if (!"ArtifactEntry".equals(oClass.getName()))
            {
                continue;
            }
            ODocument artifactCoordinates = doc.field("artifactCoordinates");
            if (artifactCoordinates == null)
            {
                continue;
            }
            String path = artifactCoordinates.field("path");
            if (path == null)
            {
                continue;
            }
            doc.field("artifactPath", path);
            result = RESULT.RECORD_CHANGED;
            logger.debug(String.format("Set 'artifactPath' value [%s] for [%s]:[%s].", path, doc.getClass(), doc.getIdentity()));
            break;
        }

        return result;
    }

}
