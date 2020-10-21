package org.carlspring.strongbox.storage.metadata.nuget.rss;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.metadata.nuget.NugetTestResourceUtil;
import org.carlspring.strongbox.storage.metadata.nuget.TempNupkgFile;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * Test class RSS
 *
 * @author sviridov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = { NugetLayoutProviderTestConfig.class })
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
public class PackageFeedTest
{

    /**
     * Recognizes the date in XMl W3C format
     *
     * @param date
     *            string with date / time
     * @return recognized date
     */
    private Date parseXmlDate(String date)
    {
        return javax.xml.bind.DatatypeConverter.parseDateTime(date).getTime();
    }

    /**
     * Verify correct package conversion to XML

import static org.assertj.core.api.Assertions.assertThat;
     *
     * @throws Exception
     */
    @Test
    public void testUnmarshallFeed()
            throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("rss/rss_feed.xml");

        // WHEN
        PackageFeed packageFeed = PackageFeed.parse(inputStream);

        // THEN
        assertThat(packageFeed.getId()).as("ID").isEqualTo("http://localhost:8090/nuget/nuget/Packages");
        assertThat(packageFeed.getUpdated()).as("Update Date").isEqualTo(parseXmlDate("2011-10-08T06:49:38Z"));
        assertThat(packageFeed.getEntries()).as("Number of packages").hasSize(26);
        assertThat(packageFeed.getTitle()).as("RSS Header").isEqualTo("Packages");
    }

    /**
     * Checking the serialization of fields in XML
     *
     * @throws Exception
     *             error during the test
     */
    @ExtendWith(ArtifactManagementTestExecutionListener.class)
    @Test
    public void testMarshallFeed(@NugetTestArtifact(id = "NUnit",
                                                    versions = "2.5.9.10348")
                                 Path artifactNupkgPath)
            throws Exception
    {
        // GIVEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(artifactNupkgPath));
             TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream))
        {
            PackageFeed feed = new PackageFeed();
            PackageEntry entry = new PackageEntry(nupkgFile);
            feed.getEntries().add(entry);

            // WHEN
            String resultXml = feed.getXml();

            // THEN
            assertThat(resultXml).contains("Packages(Id='NUnit',Version='2.5.9.10348')");
            String author = entry.getAuthor().getName();
            assertThat(resultXml).contains("name>" + author + "<");
        }
    }
}
