package org.carlspring.strongbox.nuget;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *  Extracts and stores a nuspec file
 *
 *  @author ogryniuk
 */
public class NuspecExtractor
{

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private Repository repository;

    @Inject
    private ArtifactManagementService mavenArtifactManagementService;


    /**
     *  Resolves the path and stores it with all checksums and a nuspec file
     *
     * @param streamForStorage
     * @throws IOException
     * @throws ArtifactCoordinatesValidationException
     * @throws ProviderImplementationException
     */
    // Made public for testing
    public final void storePathWithChecksumsAndNuspec(InputStream streamForStorage)
            throws IOException,
                   ArtifactCoordinatesValidationException,
                   ProviderImplementationException,
                   NugetFormatException
    {
        String repositoryPathResource = "org/carlspring/strongbox/strongbox-repositorynuspec/8.0/strongbox-nuspec-8.0.jar";

        // Issue 1146 step 2. Resolving and storing the repository path
        RepositoryPath resolvedRepositoryPath = repositoryPathResolver.resolve(repository).resolve(repositoryPathResource);
        mavenArtifactManagementService.validateAndStore(resolvedRepositoryPath, streamForStorage);

        ArtifactEntry artifactEntry = resolvedRepositoryPath.getArtifactEntry();

        // Issue 1146 step 3. Getting checksums
        Map<String, String> checksums = artifactEntry.getChecksums();

        // Issue 1146 step 4. Storing all checksums (see the invoked method)
        convertToInputStreamAndStore(resolvedRepositoryPath, checksums);

        // Issue 1146 step 5. Extracting, converting and storing a nuspecfile
        Nuspec nuspecFile = loadNuspec(streamForStorage);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
        objectOutputStream.writeObject(nuspecFile);
        objectOutputStream.flush();
        objectOutputStream.close();
        InputStream nuspecInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());

        mavenArtifactManagementService.validateAndStore(resolvedRepositoryPath, nuspecInputStream);

        // Issue 1146 step 6. Storing the nuspec checksum
        Map<String, String> nuspecChecksum = artifactEntry.getChecksums();
        convertToInputStreamAndStore(resolvedRepositoryPath, nuspecChecksum);
    }

    /**
     * Iterates through checksum maps and stores them
     *
     * @param path
     * @param mapToProcess
     * @throws ProviderImplementationException
     * @throws ArtifactCoordinatesValidationException
     * @throws IOException
     */
    private final void convertToInputStreamAndStore(RepositoryPath path, Map mapToProcess)
            throws ProviderImplementationException,
                   ArtifactCoordinatesValidationException,
                   IOException
    {
        RepositoryPath repositoryPath = path;
        Map<String, String> checksum = mapToProcess;
        InputStream completeChecksum;

        for(Map.Entry<String, String> mapEntry: checksum.entrySet()){
            InputStream checksumKey = new ByteArrayInputStream(mapEntry.getKey().getBytes(StandardCharsets.UTF_8));
            InputStream checksumValue = new ByteArrayInputStream(mapEntry.getValue().getBytes(StandardCharsets.UTF_8));
            completeChecksum = new SequenceInputStream(checksumKey, checksumValue);

            mavenArtifactManagementService.validateAndStore(repositoryPath, completeChecksum);
        }
    }


    /**
     * Retrieves and stores a nuspec file
     *
     * @param packageStream
     * @return parsed nuspec file
     * @throws IOException
     * @throws NugetFormatException
     */
    // Issue 1146 step 1. The logic of TempNupkgFile.loadNuspec moved into a new class

    // Made public for testing
    public final Nuspec loadNuspec(InputStream packageStream)
            throws IOException,
                   NugetFormatException
    {
        try (ZipInputStream zipInputStream = new ZipInputStream(packageStream);)
        {
            ZipEntry entry;
            do
            {
                entry = zipInputStream.getNextEntry();
            } while (entry != null && !isNuspecZipEntry(entry));

            if (entry == null)
            {
                return null;
            }

            return Nuspec.parse(zipInputStream);
        }
    }

    /**
     * Verifies that a ZIP attachment is Nuspec XML specification
     *
     * @param entry
     *            zip attachment
     * @return true if the attachment matches the attachment with the
     *         specification
     */
    private static final boolean isNuspecZipEntry(ZipEntry entry)
    {
        return !entry.isDirectory() && entry.getName().endsWith(Nuspec.DEFAULT_FILE_EXTENSION);
    }
}
