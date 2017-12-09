package org.carlspring.strongbox.storage.validation.groupId;

import com.google.inject.Inject;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by dinesh on 12/6/17.
 */
@Component("GroupIdCaseValidator")
public class GroupIdCaseValidator implements GroupIdValidator {

    @Inject
    Repository repository;

    @Inject
    RepositoryFileAttributes repositoryFileAttributes;

    @Inject
    MavenArtifactCoordinates coordinates;


    @Override
    public void validateGroupIdCase(RepositoryPath repositoryPath) throws GroupIdValidationException,
            ProviderImplementationException, IOException {
        RepositoryFileAttributes repositoryFileAttributes = this.getAttributes(repositoryPath);

        coordinates = (MavenArtifactCoordinates) repositoryFileAttributes.getCoordinates();
        if(!coordinates.getGroupId().toLowerCase().equals(coordinates.getGroupId())){
            throw new GroupIdValidationException("Group Id should be defined in lower case");
        }
    }

    public RepositoryFileAttributes getAttributes(RepositoryPath repositoryPath) throws IOException {
        return Files.readAttributes(repositoryPath, RepositoryFileAttributes.class);
    }


}
