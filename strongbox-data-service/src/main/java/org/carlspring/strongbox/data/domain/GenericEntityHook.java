package org.carlspring.strongbox.data.domain;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.exception.OValidationException;
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
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
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
        if ("ArtifactEntry".equals(doc.getClassName()))
        {
            validateArtifactEntryCreatedProperty(doc);
        }

        for (OClass oClass : doc.getSchemaClass().getAllSuperClasses())
        {
            if ("GenericEntity".equals(oClass.getName()))
            {
                String uuid = doc.field("uuid");
                if (uuid == null || uuid.trim().isEmpty())
                {
                    throw new OValidationException(
                            String.format("Failed to persist document [%s]. UUID can't be empty or null.",
                                          doc.getSchemaClass()));
                }
            }
            else if ("ArtifactEntry".equals(oClass.getName()))
            {
                ODocument artifactCoordinates = doc.field("artifactCoordinates");
                if (artifactCoordinates == null)
                {
                    throw new OValidationException(
                            String.format("Failed to persist document [%s]. 'artifactCoordinates' can't be null.",
                                          doc.getSchemaClass()));
                }
                
                String artifactCoordinatesPath = artifactCoordinates.field("path");

                if (artifactCoordinatesPath == null || artifactCoordinatesPath.trim().isEmpty())
                {
                    throw new OValidationException(
                            String.format("Failed to persist document [%s]. 'artifactPath' can't be empty or null.",
                                          doc.getSchemaClass()));
                }

                validateArtifactEntryCreatedProperty(doc);
            }
        }

        return result;
    }

    private void validateArtifactEntryCreatedProperty(ODocument doc) {
        Date created = doc.field("created");
        if (created == null)
        {
            throw new OValidationException(String.format("Failed to persist document [%s]. 'created' can't be null.",
                                           doc.getSchemaClass()));
        }
    }

}
