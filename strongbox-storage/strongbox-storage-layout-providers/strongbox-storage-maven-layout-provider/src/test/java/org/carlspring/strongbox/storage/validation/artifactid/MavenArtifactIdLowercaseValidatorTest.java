package org.carlspring.strongbox.storage.validation.artifactid;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.storage.validation.artifact.LowercaseValidationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by dinesh on 12/10/17.
 */
public class MavenArtifactIdLowercaseValidatorTest
{

    @Spy
    MavenArtifactIdLowercaseValidator mavenArtifactIdLowercaseValidator = new MavenArtifactIdLowercaseValidator();

    MavenArtifactCoordinates mavenArtifactCoordinates;

    @Mock
    RepositoryFileAttributes repositoryFileAttributes;

    @Before
    public void setUp()
    {
        initMocks(this);
        mavenArtifactCoordinates = new MavenArtifactCoordinates("org.dinesh.artifact.is.uppercase",
                                                                "my-maven-Artifact",
                                                                "1.0",
                                                                "classfier",
                                                                "extension");
        // repositoryFileSystem
    }

    @Test(expected = LowercaseValidationException.class)
    public void validateGroupIdCase()
            throws Exception
    {
        doReturn(repositoryFileAttributes).when(mavenArtifactIdLowercaseValidator).getAttributes(any());
        when(repositoryFileAttributes.getCoordinates()).thenReturn(mavenArtifactCoordinates);
        mavenArtifactIdLowercaseValidator.validate(null, mavenArtifactCoordinates);
    }

}