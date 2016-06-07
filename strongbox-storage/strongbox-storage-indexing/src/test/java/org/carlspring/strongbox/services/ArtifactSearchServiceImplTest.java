package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactSearchServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{
    private static final Logger logger = LoggerFactory.getLogger(ArtifactSearchServiceImplTest.class);

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Before
    public void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-utils:1.0.1:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-utils:1.1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-utils:1.2.1:jar");

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact1);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact2);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact3);

    }

    @Test
    public void testContains() throws Exception
    {
        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndex("storage0:releases");

        final int x = repositoryIndexer.index(new File("org/carlspring/strongbox/strongbox-utils"));

        Assert.assertTrue("Incorrect number of artifacts found!", x >= 3);

        SearchRequest request = new SearchRequest("storage0",
                                                  "releases",
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:1.0.1 +p:jar");

        artifactSearchService.contains(request);
    }

}
