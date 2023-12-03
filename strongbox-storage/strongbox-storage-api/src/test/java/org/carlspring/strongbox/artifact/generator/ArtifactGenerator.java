package org.carlspring.strongbox.artifact.generator;

import org.carlspring.strongbox.testing.artifact.LicenseConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * @author sbespalov
 */
public interface ArtifactGenerator
{

    int DEFAULT_BYTES_SIZE = 1000000;

    /**
     * Generate artifact with POM and checksums. Adds additional file with random content with given size inside JAR.
     * Whole size of artifact will be larger than given size.
     * @param id {@link String} id of artifact - should contains minimum one ':'
     * @param version {@link String} version of artifact, could contains 'SNAPSHOT` phrase
     * @param size additional size of file inside artifact
     * @return {@link Path} path to the artifact
     * @throws IOException in case of failure
     */
    Path generateArtifact(String id,
                          String version,
                          long size)
        throws IOException;

    /**
     * Generate artifact with POM and checksums. Adds additional file with random content with given size inside JAR.
     * Whole size of artifact will be larger than given size.
     * @param uri {@link URI} URI of artifact
     * @param size additional size of file inside artifact
     * @return {@link Path} path to the artifact
     * @throws IOException in case of failure
     */
    Path generateArtifact(URI uri,
                          long size)
        throws IOException;
    
    void setLicenses(LicenseConfiguration[] licenses)
        throws IOException;

    default void copyLicenseFile(String licenseFileSourcePath,
                                 OutputStream os)
            throws IOException
    {
        ClassPathResource resource = new ClassPathResource(licenseFileSourcePath, this.getClass().getClassLoader());

        IOUtils.copy(resource.getInputStream(), os);
    }

    default byte[] getLicenseFileContent(String licenseFileSourcePath) 
            throws IOException
    {
        ClassPathResource resource = new ClassPathResource(licenseFileSourcePath, this.getClass().getClassLoader());

        return IOUtils.toByteArray(resource.getInputStream());
    }
    
    /**
     * Added this to handle instantiation of TarArchiveEntry with only file name.
     * When TarArchiveEntry is created with only file name it considers it's size as zero, so size has to be set explicitly.
     * @param licenseConfiguration
     * @return size of the license file in bytes
     * @throws IOException
     */
    default long getLicenseFileSize(LicenseConfiguration licenseConfiguration)
            throws IOException
    {
        ClassPathResource resource = new ClassPathResource(licenseConfiguration.license().getLicenseFileSourcePath(),
                                                           this.getClass().getClassLoader());

        return IOUtils.toByteArray(resource.getInputStream()).length;
    }

}
