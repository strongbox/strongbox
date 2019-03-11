package org.carlspring.strongbox.nuget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.semver.Version;

public class NupkgFileTest
{

    @Test
    public void testHashTempFile() throws Exception
    {
        // GIVEN
        try (InputStream inputStream = NugetTestResources.getAsStream("NUnit.2.5.9.10348.nupkg"))
        {
            // WHEN
            NupkgFile nupkgFile = new NupkgFile(inputStream);
            // THEN
            assertEquals("kDPZtMu1BOZerHZvsbPnj7" + "DfOdEyn/j4fanlv7BWuuVOZ0+VwuuxWzUnpD7jo7pkLjFOqIs41Vkk7abFZj"
                    + "PRJA==", nupkgFile.getHash().toString(), "Hash file created from stream");
        }
    }

    /**
     * Check reading specifications
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testGetNuspecTmpFile() throws Exception
    {
        // GIVEN
        try (InputStream inputStream = NugetTestResources.getAsStream("NUnit.2.5.9.10348.nupkg"))
        {
            // WHEN
            NupkgFile nupkgFile = new NupkgFile(inputStream);
            Nuspec nuspecFile = nupkgFile.getNuspec();
            // THEN
            assertNotNull(nuspecFile, "Package Specification");
            assertEquals("Пакет модульного тестирования", nuspecFile.getDescription(), "Package Description");
            assertEquals("NUnit", nuspecFile.getId(), "Package ID");
            assertEquals(Version.parse("2.5.9.10348"), nuspecFile.getVersion(), "Package Version");
        }
    }

    /**
     * @throws IOException
     *             error read test data
     * @throws NugetFormatException
     *             invalid format exception
     */
    @Test
    public void testReadNupkgV3() throws IOException, NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResources.getAsStream("nupkg/v3/Package.1.0.0.nupkg");
        // WHEN
        NupkgFile nupkgFile = new NupkgFile(inputStream);
        // THEN
        assertNotNull(nupkgFile.getNuspec());
    }

    /**
     * @throws IOException
     *             error read test data
     * @throws NugetFormatException
     *             invalid format exception
     */
    @Test
    public void testReadNupkg() throws IOException, NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResources.getAsStream("nupkg/winscp.5.9.6.nupkg");
        // WHEN
        NupkgFile nupkgFile = new NupkgFile(inputStream);
        // THEN
        assertNotNull(nupkgFile.getNuspec());
    }
}
