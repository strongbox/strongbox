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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class RSS
 *
 * @author sviridov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = { NugetLayoutProviderTestConfig.class })
@SpringBootTest
@ActiveProfiles(profiles = "test")
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
        assertEquals("http://localhost:8090/nuget/nuget/Packages", packageFeed.getId(), "ID");
        assertEquals(parseXmlDate("2011-10-08T06:49:38Z"), packageFeed.getUpdated(), "Update Date");
        assertEquals(26, packageFeed.getEntries().size(), "Number of packages");
        assertEquals("Packages", packageFeed.getTitle(), "RSS Header");
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
            assertThat(resultXml, containsString("Packages(Id='NUnit',Version='2.5.9.10348')"));
            String author = entry.getAuthor().getName();
            assertThat(resultXml, containsString("name>" + author + "<"));
        }
    }
}
