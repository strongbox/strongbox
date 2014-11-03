package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.configuration.AnonymousAccessConfiguration;
import org.carlspring.strongbox.configuration.AuthenticationConfiguration;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private GenericParser<AuthenticationConfiguration> parser = new GenericParser<AuthenticationConfiguration>(AuthenticationConfiguration.class);


    @Test
    public void testParseAuthenticationConfiguration()
            throws IOException, JAXBException
    {
        File xmlFile = new File(XML_FILE);

        System.out.println("Parsing " + xmlFile.getAbsolutePath() + "...");

        //noinspection unchecked
        AuthenticationConfiguration configuration = (AuthenticationConfiguration) parser.parse(xmlFile);

        assertTrue("Failed to parse the authorization configuration!", configuration != null);
    }

    @Test
    public void testStoreAuthenticationConfiguration()
            throws IOException, JAXBException
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

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue("Failed to store the produced XML!", outputFile.length() > 0);
    }

}
