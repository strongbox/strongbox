package org.carlspring.strongbox;

import org.carlspring.strongbox.ext.StrongboxODatabaseExport;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.console.OConsoleDatabaseApp;
import com.orientechnologies.orient.core.db.tool.ODatabaseExportException;
import org.apache.commons.cli.*;

/**
 * @author Przemyslaw Fusik
 * <p>
 * http://orientdb.com/docs/3.0.x/console/Console-Command-Export.html
 */
public class OrientDbExport
        extends OConsoleDatabaseApp
{

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String FILE_NAME = "fileName";
    private static final String URL = "url";
    private static final String INCLUDE_RECORDS_CLUSTERS = "includeRecordsClusters";

    public OrientDbExport(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        OrientDbExport orientDbExport = new OrientDbExport(args);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try
        {
            cmd = parser.parse(prepareOptions(), args);
        }
        catch (ParseException e)
        {
            orientDbExport.printError(e);
            return;
        }

        try
        {
            orientDbExport.connect(cmd.getOptionValue(URL),
                                   cmd.getOptionValue(USERNAME),
                                   cmd.getOptionValue(PASSWORD));
        }
        catch (IOException e)
        {
            orientDbExport.printError(e);
            return;
        }

        orientDbExport.exportDatabase(cmd);

        orientDbExport.disconnect();
    }

    /**
     * Not included supported options: from http://orientdb.com/docs/3.0.x/console/Console-Command-Export.html
     */
    private static Options prepareOptions()
    {
        Options options = new Options();

        Option includeRecordsClusters = new Option(INCLUDE_RECORDS_CLUSTERS,
                                                   INCLUDE_RECORDS_CLUSTERS,
                                                   true,
                                                   "Records of which clusters will be exported. Separated by comma.");
        options.addOption(includeRecordsClusters);

        Option url = new Option(URL,
                                URL,
                                true,
                                "The url of the remote server or the database to connect to in the format '<mode>:<path>'");
        url.setRequired(true);
        options.addOption(url);

        Option username = new Option("u", USERNAME, true, "User name");
        username.setRequired(true);
        options.addOption(username);

        Option password = new Option("p", PASSWORD, true, "User password");
        password.setRequired(true);
        options.addOption(password);

        Option fileName = new Option("f", FILE_NAME, true, "Export destination");
        fileName.setRequired(true);
        options.addOption(fileName);

        return options;
    }

    /**
     * @see OConsoleDatabaseApp#exportDatabase(java.lang.String)
     */
    public void exportDatabase(final CommandLine cmd)

    {
        checkForDatabase();

        String fileName = cmd.getOptionValue(FILE_NAME);

        out.println(new StringBuilder("\nExporting current database to: ").append(fileName).append(
                " in GZipped JSON format ..."));

        StrongboxODatabaseExport export = null;
        try
        {
            export = new StrongboxODatabaseExport(currentDatabase, fileName, this);
            export.setIncludeRecordsClusters(getIncludeRecordsClusters(cmd));
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

    private Set<String> getIncludeRecordsClusters(CommandLine cmd)
    {
        Set<String> result = null;
        String includeRecordsClusters = cmd.getOptionValue(INCLUDE_RECORDS_CLUSTERS);
        if (includeRecordsClusters != null)
        {
            result = new HashSet<>(Arrays.asList(includeRecordsClusters.split(",")));
        }
        return result;
    }
}
