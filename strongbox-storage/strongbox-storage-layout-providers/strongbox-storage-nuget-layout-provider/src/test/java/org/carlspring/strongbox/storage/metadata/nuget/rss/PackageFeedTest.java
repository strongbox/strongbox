package org.carlspring.strongbox.storage.metadata.nuget.rss;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.NugetBootersTestConfig;
import org.carlspring.strongbox.storage.metadata.nuget.NugetTestResourceUtil;
import org.carlspring.strongbox.storage.metadata.nuget.TempNupkgFile;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Test class RSS
 *
 * @author sviridov
 */
@ContextConfiguration(classes = { NugetBootersTestConfig.class })
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class PackageFeedTest
{

    private String baseDirectoryPath;

    @Inject
    private PropertiesBooter propertiesBooter;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        baseDirectoryPath = propertiesBooter.getHomeDirectory() + "/tmp/pft";

        File baseDirectory = getCleanBaseDirectory();
        baseDirectory.mkdirs();
    }

    @AfterEach
    public void tearDown()
        throws IOException,
        JAXBException
    {
        getCleanBaseDirectory();
    }

    private File getCleanBaseDirectory()
        throws IOException
    {
        File baseDirectory = new File(baseDirectoryPath);

        if (baseDirectory.exists())
        {
            FileUtils.deleteDirectory(baseDirectory);
        }

        return baseDirectory;
    }

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
    @Test
    public void testMarshallFeed()
        throws Exception
    {
        // GIVEN
        String packageId = "NUnit";
        String packageVersion = "2.5.9.10348";
        Path packageFilePath = TestCaseWithNugetPackageGeneration.generatePackageFile(baseDirectoryPath,
                                                                                      packageId,
                                                                                      packageVersion,
                                                                                      (String[]) null/*
                                                                                                      * dependencyList
                                                                                                      */);

        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(packageFilePath));
                TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream);)
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
