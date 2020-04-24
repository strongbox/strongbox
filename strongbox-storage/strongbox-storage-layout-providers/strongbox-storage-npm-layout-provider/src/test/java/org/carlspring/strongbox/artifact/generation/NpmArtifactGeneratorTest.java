package org.carlspring.strongbox.artifact.generation;

import org.carlspring.strongbox.config.NpmLayoutProviderTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;
import org.carlspring.strongbox.testing.artifact.LicenseType;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.carlspring.strongbox.util.MessageDigestUtils.readChecksumFile;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Wojciech Pater
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NpmLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
class NpmArtifactGeneratorTest
{

    private static final String REPOSITORY_RELEASES = "natg-releases";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void testArtifactGeneration(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                Repository repository,
                                @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                 id = "npm-test-view",
                                                 versions = "1.0.0",
                                                 scope = "@carlspring",
                                                 bytesSize = 2048,
                                                 licenses = {@LicenseConfiguration(license = LicenseType.APACHE_2_0,
                                                                                   destinationPath = "LICENSE-Apache-2.0.md"),
                                                             @LicenseConfiguration(license = LicenseType.MIT)})
                                                		
                                Path path)
            throws Exception
    {
        assertThat(Files.exists(path)).as("Failed to generate NPM package.").isTrue();

        // SHA1
        String fileName = path.getFileName().toString();
        String checksumFileName = fileName + ".sha1";
        Path pathSha1 = path.resolveSibling(checksumFileName);
        assertThat(Files.exists(pathSha1)).as("Failed to generate NPM SHA1 file.").isTrue();

        String expectedSha1 = calculateChecksum(path, MessageDigestAlgorithms.SHA_1);
        String result = readChecksumFile(pathSha1.toString());
        assertThat(result).isEqualTo(expectedSha1);

        // File size
        assertThat(Files.size(path)).isGreaterThan(2048);
        
        // License checks
       checkLicenses(path);
    }
    
    public void checkLicenses(Path artifactPath)
            throws IOException, ParseException
    {
    	//1) Check that the license are located in the expected locations
    	
    	 assertThat(containsLicense(artifactPath, "LICENSE"))
         .as("Did not find a license that was expected at the default location!")
         .isTrue();
    	 
    	 assertThat(containsLicense(artifactPath, "LICENSE-Apache-2.0.md"))
         .as("Did not find a license that was expected at the specified location!")
         .isTrue();
    	 
    	// 3) Check that the package.json contains the list of licenses
    	 
    	 String jsonPath = artifactPath.getParent().toString() +"/package.json";
    	 File json = new File(jsonPath);
    	 JSONParser parser = new JSONParser(); 
    	 FileReader reader = new FileReader(json.getAbsolutePath());
    	 Object obj = parser.parse(reader);
    	 JSONObject jsonObj = (JSONObject) obj;
    	 JSONArray licenses = (JSONArray)jsonObj.get("licenses");
    	 
    	 assertThat(licenses.isEmpty())
    	 .as("Could not discover any licenses in the package.json file!")
    	 .isFalse();
    	 
    	 assertThat(licenses.get(0).toString().contains("Apache 2.0"))
    	 .as("Failed to locate a definition for the 'Apache 2.0' license in the package.json file!")
    	 .isTrue();
    	 
    	 assertThat(licenses.get(1).toString().contains("MIT"))
    	 .as("Failed to locate a definition for the 'Apache 2.0' license in the package.json file!")
    	 .isTrue();
    		 
    	 }
    
    public boolean containsLicense(Path path, String s)
    		throws FileNotFoundException, IOException {
    	TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(path.toString())));
    	TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
    	while (currentEntry != null) {
    	    if (currentEntry.getName().equals(s)) {
    	    	tarInput.close();
    	    	return true;
    	    }
    	    currentEntry = tarInput.getNextTarEntry();
    	}
    	tarInput.close();
    	return false;
    	
    }

    	 
}
