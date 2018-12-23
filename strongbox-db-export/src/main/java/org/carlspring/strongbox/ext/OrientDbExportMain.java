package org.carlspring.strongbox.ext;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.*;

/**
 * @author Przemyslaw Fusik
 * <p>
 */
public class OrientDbExportMain
{

    private static final String URL = "url";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String FILE_NAME = "fileName";
    private static final String INCLUDE_RECORDS_CLUSTERS = "includeRecordsClusters";

    public static void main(String[] args)
    {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try
        {
            cmd = parser.parse(prepareOptions(), args);
        }
        catch (ParseException e)
        {
            e.printStackTrace(System.err);
            return;
        }

        StrongboxOConsoleDatabaseApp orientDbExport = new StrongboxOConsoleDatabaseApp(args);
        orientDbExport.setIncludeRecordsClusters(
                getIncludeRecordsClusters(cmd.getOptionValue(INCLUDE_RECORDS_CLUSTERS)));

        try
        {
            orientDbExport.connect(cmd.getOptionValue(URL),
                                   cmd.getOptionValue(USERNAME),
                                   cmd.getOptionValue(PASSWORD));
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
            return;
        }

        orientDbExport.exportDatabase(cmd.getOptionValue(FILE_NAME));

        orientDbExport.disconnect();
    }

    /**
     * Extend for your needs: http://orientdb.com/docs/3.0.x/console/Console-Command-Export.html
     */
    private static Options prepareOptions()
    {
        Options options = new Options();

        Option includeRecordsClusters = new Option("i",
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

    private static Set<String> getIncludeRecordsClusters(String includeRecordsClusters)
    {
        Set<String> result = null;
        if (includeRecordsClusters != null)
        {
            result = new HashSet<>(Arrays.asList(includeRecordsClusters.split(",")));
        }
        return result;
    }


}
