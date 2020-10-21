package org.carlspring.strongbox.storage.metadata.maven.io.filters;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mtodorov
 */
public class ArtifactVersionDirectoryFilterTest
{

    public static final Path BASEDIR = Paths.get("target").resolve("test-resources");


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
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
            throws IOException
    {
        List<Path> paths;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(BASEDIR.resolve("com").resolve("foo").resolve("bar"),
                                                                 new ArtifactVersionDirectoryFilter()))
        {
            paths = Lists.newArrayList(ds);
        }

        assertThat(paths).as("Expected versions to be discovered.").isNotNull();
        assertThat(paths).as("Expected three versions.").hasSize(3);
    }

}
