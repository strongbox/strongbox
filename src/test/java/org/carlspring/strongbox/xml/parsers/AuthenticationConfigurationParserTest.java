package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.configuration.AnonymousAccessConfiguration;
import org.carlspring.strongbox.configuration.AuthenticationConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class AuthenticationConfigurationParserTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/etc/conf";

    public static final String XML_FILE = CONFIGURATION_BASEDIR + "/security-authentication.xml";

    public static final String XML_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/security-authentication-saved.xml";


    @Test
    public void testParseAuthenticationConfiguration()
            throws IOException
    {
        File xmlFile = new File(XML_FILE);

        System.out.println("Parsing " + xmlFile.getAbsolutePath() + "...");

        AuthenticationConfigurationParser parser = new AuthenticationConfigurationParser();
        final XStream xstream = parser.getXStreamInstance();

        //noinspection unchecked
        AuthenticationConfiguration configuration = (AuthenticationConfiguration) xstream.fromXML(xmlFile);

        assertTrue("Failed to parse the authorization configuration!", configuration != null);
    }

    @Test
    public void testStoreAuthenticationConfiguration()
            throws IOException
    {
        List<String> realms = new ArrayList<String>();
        realms.add("org.carlspring.strongbox.jaas.xml.XMLUserRealm");
        realms.add("org.carlspring.strongbox.jaas.xml.LDAPUserRealm");
        realms.add("org.carlspring.strongbox.jaas.xml.ADUserRealm");
        realms.add("org.carlspring.strongbox.jaas.xml.RDBMSUserRealm");

        AnonymousAccessConfiguration anonymousAccessConfiguration = new AnonymousAccessConfiguration();

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setAnonymousAccessConfiguration(anonymousAccessConfiguration);
        configuration.setRealms(realms);

        File outputFile = new File(XML_OUTPUT_FILE).getAbsoluteFile();

        System.out.println("Storing " + outputFile.getAbsolutePath() + "...");

        AuthenticationConfigurationParser parser = new AuthenticationConfigurationParser();
        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue("Failed to store the produced XML!", outputFile.length() > 0);
    }

}
