package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.npm.metadata.Dist;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.util.StrongboxUriComponentsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 *
 */
@Component
public class NpmPackageSupplier implements Function<Path, NpmPackageDesc>
{
    
    private static final Logger logger = LoggerFactory.getLogger(NpmPackageSupplier.class);
    
    @Inject
    private NpmLayoutProvider layoutProvider;

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    StrongboxUriComponentsBuilder uriBuilder;

    @Override
    public NpmPackageDesc apply(Path path)
    {
        RepositoryPath repositoryPath = (RepositoryPath) path;

        NpmFileSystemProvider npmFileSystemProvider = (NpmFileSystemProvider) path.getFileSystem().provider();

        NpmArtifactCoordinates c;
        ArtifactEntry artifactEntry;
        try
        {
            c = (NpmArtifactCoordinates) RepositoryFiles.readCoordinates(repositoryPath);
            artifactEntry = repositoryPath.getArtifactEntry();
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        NpmPackageDesc npmPackageDesc = new NpmPackageDesc();
        npmPackageDesc.setReleaseDate(artifactEntry.getLastUpdated());

        PackageVersion npmPackage = new PackageVersion();
        npmPackageDesc.setNpmPackage(npmPackage);

        npmPackage.setAdditionalProperty("_id", String.format("%s@%s", c.getId(), c.getVersion()));
        
        npmPackage.setName(c.getId());
        npmPackage.setVersion(c.getVersion());
        
        Dist dist = new Dist();
        npmPackage.setDist(dist);

        Map<String, RepositoryPath> checksumMap = npmFileSystemProvider.resolveChecksumPathMap(repositoryPath);
        fetchShasum(dist, checksumMap);

        String url;
        try
        {
            url = layoutProvider.resolveResourceURL(repositoryPath).toString();
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        dist.setTarball(url);

        if (artifactEntry.getTagSet().contains(artifactTagService.findOneOrCreate(ArtifactTag.LAST_VERSION)))
        {
            npmPackageDesc.setLastVersion(true);
        }

        return npmPackageDesc;
    }

    private void fetchShasum(Dist dist,
                             Map<String, RepositoryPath> checksumMap)
    {
        RepositoryPath shasumPath = checksumMap.get(MessageDigestAlgorithms.SHA_1);
        if (shasumPath == null)
        {
            return;
        }

        try
        {
            dist.setShasum(new String(Files.readAllBytes(shasumPath), "UTF-8").trim());
        } 
        catch (NoSuchFileException e) 
        {
            logger.warn("Checksum file not found [{}].", shasumPath);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

}
