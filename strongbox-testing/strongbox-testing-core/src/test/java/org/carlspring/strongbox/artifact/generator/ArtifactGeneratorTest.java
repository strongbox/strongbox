package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.junit.Test;
import static junit.framework.TestCase.assertTrue;

/**
 * @author mtodorov
 */
public class ArtifactGeneratorTest
{

    private final static String BASEDIR = "target/strongbox/storages/storage0/releases";


    @Test
    public void testArtifactGeneration()
            throws Exception
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.testing:test-foo:1.2.3:jar");

        ArtifactGenerator generator = new ArtifactGenerator(BASEDIR);
        generator.generate(artifact);

        assertTrue("Failed to generate POM file!",
                   new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.pom").exists());
    }

}
