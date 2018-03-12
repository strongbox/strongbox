package org.carlspring.strongbox.io.filters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author mtodorov
 */
public class ArtifactVersionDirectoryFilterTest
{

    public static final Path BASEDIR = Paths.get("target/test-resources");


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp()
            throws Exception
    {
        File basedir = new File(BASEDIR.toFile(), "com/foo/bar");
        if (!basedir.exists())
        {
            basedir.mkdirs();

            new File(basedir, "1.2.1").mkdirs();
            new File(basedir, "1.2.2").mkdirs();
            new File(basedir, "1.2.3").mkdirs();
            new File(basedir, "blah").mkdirs();

            new File(basedir, "1.2.1/bar-1.2.1.pom").createNewFile();
            new File(basedir, "1.2.2/bar-1.2.2.pom").createNewFile();
            new File(basedir, "1.2.3/bar-1.2.3.pom").createNewFile();
        }
    }

    @Test
    public void testFiltering()
    {
        File[] files = new File(BASEDIR.toFile(), "com/foo/bar").listFiles(new ArtifactVersionDirectoryFilter());

        assertNotNull("Expected versions to be discovered.", files.length);
        assertEquals("Expected three versions.", 3, files.length);
    }

}
