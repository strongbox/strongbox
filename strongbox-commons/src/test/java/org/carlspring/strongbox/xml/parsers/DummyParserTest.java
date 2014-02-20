package org.carlspring.strongbox.xml.parsers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class DummyParserTest
{

    private File testResourcesDir = new File("target/test-resources");

    private Dummy dummy;

    private DummyParser parser;


    @Before
    public void setUp()
            throws Exception
    {
        parser = new DummyParser();

        dummy = new Dummy();
        dummy.setName("foo");
        dummy.addAlias("bar");
        dummy.addAlias("blah");

        if (!testResourcesDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            testResourcesDir.mkdirs();
        }
    }

    @Test
    public void testStore()
            throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final File outputFile = new File(testResourcesDir, "dummy.xml");

        parser.store(dummy, outputFile);
        parser.store(dummy, System.out);
        parser.store(dummy, baos);

        String result = new String(baos.toByteArray());
        assertTrue("Failed to store output file!", outputFile.exists());

        assertEquals("<name>\n" +
                     "    <name>foo</name>\n" +
                     "    <aliases class=\"linked-hash-set\">\n" +
                     "        <string>bar</string>\n" +
                     "        <string>blah</string>\n" +
                     "    </aliases>\n" +
                     "</name>",
                     result);
    }

    @Test()
    public void testParse()
            throws IOException
    {
        final File file = new File(testResourcesDir, "dummy.xml");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        parser.store(dummy, file);
        parser.store(dummy, baos);

        // Parse from file
        final Dummy dummyParsedFromFile = parser.parse(file);

        assertEquals(dummy, dummyParsedFromFile);

        // Parse from InputStream
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final Dummy dummyParsedFromInputStream = parser.parse(bais);

        assertEquals(dummy, dummyParsedFromInputStream);

        // Parse from URL from local fs
        URL url1 = file.getCanonicalFile().toURI().toURL();
        final Dummy dummyParserFromURL = parser.parse(url1);
        assertEquals(dummyParsedFromFile, dummyParserFromURL);

        // Parse from URL from classpath
        URL url2 = new URL("classpath:META-INF/spring/dummy.xml");
        final Dummy dummyParserFromClasspath = parser.parse(url2);
        assertEquals(dummyParsedFromFile, dummyParserFromClasspath);
    }

}
