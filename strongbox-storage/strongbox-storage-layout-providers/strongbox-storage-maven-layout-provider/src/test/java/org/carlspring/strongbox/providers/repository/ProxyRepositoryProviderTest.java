package org.carlspring.strongbox.providers.repository;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProxyRepositoryProviderTest
{

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox.artifact",
                                    "org.carlspring.strongbox.configuration",
                                    "org.carlspring.strongbox.io",
                                    "org.carlspring.strongbox.providers",
                                    "org.carlspring.strongbox.repository",
                                    "org.carlspring.strongbox.services",
                                    "org.carlspring.strongbox.storage",
                                    "org.carlspring.strongbox.xml" })
    public static class SpringConfig
    {

    }

    @Inject
    private ArtifactResolutionService artifactResolutionService;


    @Before
    public void setUp()
            throws Exception
    {
        File derbyPluginBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                           "/storages/storage-common-proxies/maven-central/" +
                                           "org/carlspring/maven/derby-maven-plugin");
        if (derbyPluginBaseDir.exists())
        {
            FileUtils.deleteDirectory(derbyPluginBaseDir);
        }
    }

    @Test
    public void testMavenCentral()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException
    {
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml");

        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.md5");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.sha1");

        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.md5");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.sha1");
    }

    private void assertStreamNotNull(String storageId, String repositoryId, String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        InputStream is = artifactResolutionService.getInputStream(storageId, repositoryId, path);

        assertNotNull("Failed to resolve " + path + "!", is);

        if (ArtifactUtils.isMetadata(path))
        {
            System.out.println(ByteStreams.toByteArray(is));
        }
    }

    @Ignore // Broken while Docker is being worked on, as there is no running instance of the Strongbox service.
    @Test
    public void testStrongboxAtCarlspringDotOrg()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException
    {
        InputStream is = artifactResolutionService.getInputStream("public",
                                                                  "public-group",
                                                                  "org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml");

        assertNotNull("Failed to resolve org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml!", is);

        System.out.println(ByteStreams.toByteArray(is));
    }

}
