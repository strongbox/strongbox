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
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Dmitry Sviridov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = { NugetLayoutProviderTestConfig.class })
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
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
            assertThat(nupkgFile.getHash())
                    .as("Hash file created from stream")
                    .isNotNull();
            assertThat(nupkgFile.getHash())
                    .as("Hash file created from stream")
                    .isEqualTo(expectedHash);
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
            assertThat(nuspecFile).as("Package Specification").isNotNull();
            assertThat(nuspecFile.getId()).as("Package ID").isEqualTo(expectedPackageId);
            assertThat(nuspecFile.getVersion()).as("Package Version").isEqualTo(SemanticVersion.parse(expectedPackageVersion));
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
            assertThat(nupkgFile.getNuspec()).isNotNull();
        }
    }
}
