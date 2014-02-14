package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.jaas.Role;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class RolesParserTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/etc/conf";

    public static final String XML_FILE = CONFIGURATION_BASEDIR + "/roles.xml";

    public static final String XML_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/roles-saved.xml";


    @Test
    public void testParseRoles()
            throws IOException
    {
        File xmlFile = new File(XML_FILE);

        System.out.println("Parsing " + xmlFile.getAbsolutePath() + "...");

        RoleParser parser = new RoleParser();
        final XStream xstream = parser.getXStreamInstance();

        //noinspection unchecked
        final List<Role> roles = (List<Role>) xstream.fromXML(xmlFile);

        assertTrue("Failed to parse any roles!", roles != null);
        assertFalse("Failed to parse any roles!", roles.isEmpty());
    }

    @Test
    public void testStoreRoles()
            throws IOException
    {
        List<Role> roles = new ArrayList<Role>();
        roles.add(new Role("admin", "Admin role"));
        roles.add(new Role("deployer", "Deployment role"));

        File outputFile = new File(XML_OUTPUT_FILE).getAbsoluteFile();

        System.out.println("Storing " + outputFile.getAbsolutePath() + "...");

        RoleParser parser = new RoleParser();
        parser.store(roles, outputFile.getCanonicalPath());

        assertTrue("Failed to store the produced XML!", outputFile.length() > 0);
    }

}
