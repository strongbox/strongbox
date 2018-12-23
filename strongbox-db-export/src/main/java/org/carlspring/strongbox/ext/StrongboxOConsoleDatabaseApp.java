package org.carlspring.strongbox.ext;

import java.io.IOException;
import java.util.Set;

import com.orientechnologies.orient.console.OConsoleDatabaseApp;
import com.orientechnologies.orient.core.db.tool.ODatabaseExportException;

/**
 * @author Przemyslaw Fusik
 * <p>
 * http://orientdb.com/docs/3.0.x/console/Console-Command-Export.html
 */
public class StrongboxOConsoleDatabaseApp
        extends OConsoleDatabaseApp
{

    private Set<String> includeRecordsClusters;

    public StrongboxOConsoleDatabaseApp(String[] args)
    {
        super(args);
    }

    @Override
    public void exportDatabase(String fileName)
    {
        checkForDatabase();

        out.println(new StringBuilder("\nExporting current database to: ")
                            .append(fileName)
                            .append(" in GZipped JSON format ..."));

        StrongboxODatabaseExport export = null;
        try
        {
            export = new StrongboxODatabaseExport(currentDatabase, fileName, this);
            export.setIncludeRecordsClusters(includeRecordsClusters);
            export.exportDatabase();
        }
        catch (IOException | ODatabaseExportException e)
        {
            printError(e);
        }
        finally
        {
            if (export != null)
            {
                export.close();
            }
        }
    }

    void setIncludeRecordsClusters(Set<String> includeRecordsClusters)
    {
        this.includeRecordsClusters = includeRecordsClusters;
    }
}
