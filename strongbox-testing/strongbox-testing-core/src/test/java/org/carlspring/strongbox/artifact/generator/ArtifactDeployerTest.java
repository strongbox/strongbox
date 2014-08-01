package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactOperationException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mtodorov
 */
public class ArtifactDeployerTest
{

    private final static File BASEDIR = new File("target/strongbox/tmp/storages/storage0/releases");


    @Before
    public void setUp()
            throws Exception
    {
        if (!BASEDIR.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            BASEDIR.mkdirs();
        }
    }

    @Test
    public void testArtifactDeployment()
            throws ArtifactOperationException,
                   IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:test:1.2.3");

        String[] classifiers = new String[] { "javadocs", "jdk14", "tests"};

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(BASEDIR);
        artifactDeployer.generateAndDeployArtifact(artifact, classifiers, "storage0", "releases");
    }

}
