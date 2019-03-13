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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.NugetBootersTestConfig;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semver.Version;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Dmitry Sviridov
 */
@ContextConfiguration(classes = { NugetBootersTestConfig.class })
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class TempNupkgFileTest
{
    private String baseDirectoryPath;

    @Inject
    private PropertiesBooter propertiesBooter;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        baseDirectoryPath = propertiesBooter.getHomeDirectory() + "/tmp/tnft";

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

    @Test
    public void testHashTempFile()
        throws Exception
    {
        // GIVEN
        String packageId = "NUnit";
        String packageVersion = "2.5.9.10348";
        Path packageFilePath = TestCaseWithNugetPackageGeneration.generatePackageFile(baseDirectoryPath, packageId,
                                                                                      packageVersion,
                                                                                      (String[]) null/*
                                                                                                      * dependencyList
                                                                                                      */);

        // WHEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(packageFilePath));
                TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream);)
        {
            // TODO: Assert that nupkgFile.getHash() equals generated .nupkg.sha512 file content
            
            // THEN
            assertNotNull(nupkgFile.getHash().toString(),
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
        String expectedPackageId = "NUnit";
        String expectedPackageVersion = "2.5.9.10348";

        Path packageFilePath = TestCaseWithNugetPackageGeneration.generatePackageFile(baseDirectoryPath,
                                                                                      expectedPackageId,
                                                                                      expectedPackageVersion,
                                                                                      (String[]) null/*
                                                                                                      * dependencyList
                                                                                                      */);

        // WHEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(packageFilePath));
                TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream);)
        {
            // WHEN
            Nuspec nuspecFile = nupkgFile.getNuspec();
            // THEN
            assertNotNull(nuspecFile, "Package Specification");
            assertEquals(expectedPackageId, nuspecFile.getId(), "Package ID");
            assertEquals(Version.parse(expectedPackageVersion), nuspecFile.getVersion(), "Package Version");
        }
    }

    /**
     * @throws IOException
     *             error read test data
     * @throws NugetFormatException
     *             invalid format exception
     * @throws JAXBException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testReadNupkg()
        throws IOException,
        NugetFormatException,
        NoSuchAlgorithmException,
        JAXBException
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

        // WHEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(packageFilePath));
                TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream);)
        {
            // THEN
            assertNotNull(nupkgFile.getNuspec());
        }
    }
}
