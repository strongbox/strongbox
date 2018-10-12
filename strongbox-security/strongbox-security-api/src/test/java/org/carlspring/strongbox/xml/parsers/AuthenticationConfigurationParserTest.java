package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.configuration.AnonymousAccessConfiguration;
import org.carlspring.strongbox.configuration.AuthenticationConfiguration;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mtodorov
 */
public class AuthenticationConfigurationParserTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/etc/conf";

    public static final String XML_FILE = CONFIGURATION_BASEDIR + "/security-authentication.xml";

    public static final String XML_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/security-authentication-saved.xml";

    private GenericParser<AuthenticationConfiguration> parser = new GenericParser<>(AuthenticationConfiguration.class);


    @Test
    public void testParseAuthenticationConfiguration()
            throws IOException, JAXBException
    {
        File xmlFile = new File(XML_FILE);

        System.out.println("Parsing " + xmlFile.getAbsolutePath() + "...");

        //noinspection unchecked
        AuthenticationConfiguration configuration = (AuthenticationConfiguration) parser.parse(xmlFile.toURI().toURL());

        assertNotNull(configuration, "Failed to parse the authorization configuration!");
    }

    @Test
    public void testStoreAuthenticationConfiguration()
            throws IOException, JAXBException
    {
        List<String> realms = new ArrayList<>();
        realms.add("org.carlspring.strongbox.security.authentication.xml.XMLUserRealm");
        realms.add("org.carlspring.strongbox.security.authentication.ldap.LDAPUserRealm");
        realms.add("org.carlspring.strongbox.security.authentication.ad.ADUserRealm");
        realms.add("org.carlspring.strongbox.security.authentication.jdbc.JDBCUserRealm");

        AnonymousAccessConfiguration anonymousAccessConfiguration = new AnonymousAccessConfiguration();

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setAnonymousAccessConfiguration(anonymousAccessConfiguration);
        configuration.setRealms(realms);

        File outputFile = new File(XML_OUTPUT_FILE).getAbsoluteFile();

        System.out.println("Storing " + outputFile.getAbsolutePath() + "...");

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue(outputFile.length() > 0, "Failed to store the produced XML!");
    }

}
