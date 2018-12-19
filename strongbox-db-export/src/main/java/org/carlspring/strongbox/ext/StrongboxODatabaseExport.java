package org.carlspring.strongbox.ext;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.tool.ODatabaseExport;

/**
 * @author Przemyslaw Fusik
 * <p>
 * Extension to ODatabaseExport providing ability to select records of clusters to be exported.
 */
public class StrongboxODatabaseExport
        extends ODatabaseExport
{

    private Set<String> includeRecordsClusters;

    public StrongboxODatabaseExport(ODatabaseDocumentInternal iDatabase,
                                    String iFileName,
                                    OCommandOutputListener iListener)
            throws IOException
    {
        super(iDatabase, iFileName, iListener);
    }

    public void setIncludeRecordsClusters(Set<String> includeRecordsClusters)
    {
        this.includeRecordsClusters = includeRecordsClusters;
    }

    @Override
    public long exportRecords()
            throws IOException
    {
        Set<String> previousIncludeClusters = includeClusters;
        setIncludeClusters(combineIncludeClusters());
        long result = super.exportRecords();
        setIncludeClusters(previousIncludeClusters);
        return result;
    }

    private Set<String> combineIncludeClusters()
    {
        Set<String> combine = null;
        if (includeClusters != null)
        {
            combine = new HashSet<>(includeClusters);
        }
        if (includeRecordsClusters != null)
        {
            if (combine == null)
            {
                combine = new HashSet<>();
            }
            combine.addAll(includeRecordsClusters);
        }
        return combine;
    }
}
