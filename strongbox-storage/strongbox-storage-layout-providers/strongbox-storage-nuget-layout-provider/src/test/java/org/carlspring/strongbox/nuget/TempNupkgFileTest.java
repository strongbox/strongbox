/*
 * Copyright 2019 Carlspring Consulting & Development Ltd.
 * Copyright 2014 Dmitry Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carlspring.strongbox.nuget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.semver.Version;

/**
 * @author Dmitry Sviridov
 */
public class TempNupkgFileTest
{

    @Test
    public void testHashTempFile()
        throws Exception
    {
        // GIVEN
        // WHEN
        try (InputStream inputStream = NugetTestResources.getAsStream("NUnit.2.5.9.10348.nupkg");
                TempNupkgFile nupkgFile = new TempNupkgFile(inputStream);)
        {
            // THEN
            assertEquals(
                         "kDPZtMu1BOZerHZvsbPnj7DfOdEyn/j4fanlv7BWuuVOZ0+VwuuxWzUnpD7jo7pkLjFOqIs41Vkk7abFZjPRJA==",
                         nupkgFile.getHash().toString(),
                         "Hash file created from stream");
        }
    }

    /**
     * Check reading specifications
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testGetNuspecTmpFile()
        throws Exception
    {
        // GIVEN
        try (InputStream inputStream = NugetTestResources.getAsStream("NUnit.2.5.9.10348.nupkg");
                TempNupkgFile nupkgFile = new TempNupkgFile(inputStream);)
        {
            // WHEN
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
    public void testReadNupkgV3()
        throws IOException,
        NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResources.getAsStream("nupkg/v3/Package.1.0.0.nupkg");
        // WHEN
        try (TempNupkgFile nupkgFile = new TempNupkgFile(inputStream))
        {
            // THEN
            assertNotNull(nupkgFile.getNuspec());
        }
    }

    /**
     * @throws IOException
     *             error read test data
     * @throws NugetFormatException
     *             invalid format exception
     */
    @Test
    public void testReadNupkg()
        throws IOException,
        NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResources.getAsStream("nupkg/winscp.5.9.6.nupkg");
        // WHEN
        try (TempNupkgFile nupkgFile = new TempNupkgFile(inputStream))
        {
            // THEN
            assertNotNull(nupkgFile.getNuspec());
        }
    }
}
