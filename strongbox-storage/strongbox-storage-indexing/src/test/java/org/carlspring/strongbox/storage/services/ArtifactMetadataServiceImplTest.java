package org.carlspring.strongbox.storage.services;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.storage.services.impl.ArtifactMetadataServiceImpl;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.JVM)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml",
                                   "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceImplTest
{

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

    private Artifact artifact;
    private Artifact latestArtifact;
    private Artifact releaseArtifact;

    private ArtifactMetadataServiceImpl metadataService;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        REPOSITORY_BASEDIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:1.0:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:1.1:war");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:2.0:zip");
        Artifact artifact4 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-metadata:3.0-SNAPSHOT:jar");

        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
        generator.generate(artifact1);
        generator.generate(artifact2);
        generator.generate(artifact3);
        generator.generate(artifact4);

        artifact = artifact1;
        latestArtifact = artifact4;
        releaseArtifact = artifact3;
        metadataService = new ArtifactMetadataServiceImpl();

        metadataService.rebuildMetadata(artifact);

    }

    @Test
    public void testMetadataRebuild()
            throws IOException, XmlPullParserException
    {
        String artifactBasePath = metadataService.getArtifactBasePath(artifact).toString();
        File metadataFile = new File(artifactBasePath + "/maven-metadata.xml");
        Assert.assertTrue("Failed to rebuild maven-metadata.xml file in " + artifactBasePath, metadataFile.exists());
    }

    @Test
    public void testMetadataRead()
            throws IOException, XmlPullParserException
    {

        Metadata metadata = metadataService.getMetadata(artifact);
        Versioning versioning = metadata.getVersioning();

        Assert.assertEquals("Artifact Id don't match!", artifact.getArtifactId(), metadata.getArtifactId());
        Assert.assertEquals("Group Id don't match!", artifact.getGroupId(), metadata.getGroupId());
        Assert.assertEquals("Latest version doesn't match!", latestArtifact.getVersion(), versioning.getLatest());
        Assert.assertEquals("Release version don't match!", releaseArtifact.getVersion(), versioning.getRelease());

    }
}
