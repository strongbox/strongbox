package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class ArtifactGeneratorTest
        extends TestCaseWithArtifactGeneration
{

    private final static File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");


    @Test
    public void testArtifactGeneration()
            throws Exception
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.testing:test-foo:1.2.3:jar");

        generateArtifact(BASEDIR.getAbsolutePath(), artifact);

        assertTrue("Failed to generate POM file!",
                   new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.pom").exists());
    }

}
