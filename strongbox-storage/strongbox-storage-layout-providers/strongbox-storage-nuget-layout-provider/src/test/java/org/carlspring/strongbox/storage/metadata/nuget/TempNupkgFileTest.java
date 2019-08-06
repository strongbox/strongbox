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

package org.carlspring.strongbox.storage.metadata.nuget;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dmitry Sviridov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = { NugetLayoutProviderTestConfig.class })
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class TempNupkgFileTest
{

    @ExtendWith(ArtifactManagementTestExecutionListener.class)
    @Test
    public void testHashTempFile(@NugetTestArtifact(id = "NUnit",
                                                    versions = "2.5.9.10348")
                                 Path artifactNupkgPath)
            throws Exception
    {
        // GIVEN
        Path checksumPath = artifactNupkgPath.resolveSibling(artifactNupkgPath.getFileName() + ".sha512");

        String expectedHash = MessageDigestUtils.readChecksumFile(checksumPath.toString());

        // WHEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(artifactNupkgPath));
             TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream))
        {
            // THEN
            assertNotNull(nupkgFile.getHash(),
                          "Hash file created from stream");
            assertEquals(expectedHash,
                         nupkgFile.getHash(),
                         "Hash file created from stream");
        }
    }

    /**
     * Check reading specifications
     *
     * @throws Exception error during the test
     */
    @ExtendWith(ArtifactManagementTestExecutionListener.class)
    @Test
    public void testGetNuspecTmpFile(@NugetTestArtifact(id = "NUnit",
                                                        versions = "2.5.9.10348")
                                     Path artifactNupkgPath)
            throws Exception
    {
        // GIVEN
        String expectedPackageId = "NUnit";
        String expectedPackageVersion = "2.5.9.10348";

        // WHEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(artifactNupkgPath));
             TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream))
        {
            // WHEN
            Nuspec nuspecFile = nupkgFile.getNuspec();

            // THEN
            assertNotNull(nuspecFile, "Package Specification");
            assertEquals(expectedPackageId, nuspecFile.getId(), "Package ID");
            assertEquals(SemanticVersion.parse(expectedPackageVersion), nuspecFile.getVersion(), "Package Version");
        }
    }

    /**
     * @throws IOException          error read test data
     * @throws NugetFormatException invalid format exception
     */
    @ExtendWith(ArtifactManagementTestExecutionListener.class)
    @Test
    public void testReadNupkg(@NugetTestArtifact(id = "NUnit",
                                                 versions = "2.5.9.10348")
                              Path artifactNupkgPath)
            throws IOException,
                   NugetFormatException
    {
        // WHEN
        try (InputStream nupkgInputStream = new BufferedInputStream(Files.newInputStream(artifactNupkgPath));
             TempNupkgFile nupkgFile = new TempNupkgFile(nupkgInputStream))
        {
            // THEN
            assertNotNull(nupkgFile.getNuspec());
        }
    }
}
