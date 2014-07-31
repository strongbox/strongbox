package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactOperationException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(BASEDIR);
        artifactDeployer.generateAndDeployArtifact(artifact, "storage0", "releases");
    }

}
