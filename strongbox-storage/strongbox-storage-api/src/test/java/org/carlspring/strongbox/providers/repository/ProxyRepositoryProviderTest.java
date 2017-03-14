package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ArtifactResolutionService;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteStreams;
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


    @Test
    public void testMavenCentral()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException
    {
        InputStream is = artifactResolutionService.getInputStream("storage-common-proxies",
                                                                  "maven-central",
                                                                  "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml");

        assertNotNull("Failed to resolve org/carlspring/maven/derby-maven-plugin/maven-metadata.xml!", is);

        System.out.println(ByteStreams.toByteArray(is));
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
