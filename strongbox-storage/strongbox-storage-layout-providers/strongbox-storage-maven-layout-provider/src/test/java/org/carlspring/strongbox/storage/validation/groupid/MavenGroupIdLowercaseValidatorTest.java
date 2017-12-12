package org.carlspring.strongbox.storage.validation.groupid;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


/**
 * Created by dinesh on 12/7/17.
 */

public class MavenGroupIdLowercaseValidatorTest
{

    @Spy
    MavenGroupIdLowercaseValidator groupIdCaseValidator = new MavenGroupIdLowercaseValidator();

    MavenArtifactCoordinates mavenArtifactCoordinates;
    @Mock
    RepositoryPath repositoryPath = new RepositoryPath(null, null);

    @Mock
    RepositoryFileAttributes repositoryFileAttributes;
    
    @Before
    public void setUp()
    {
        initMocks(this);
        mavenArtifactCoordinates = new MavenArtifactCoordinates("org.dinesh.artifact.is.Uppercase",
                                                                "my-maven-artifact",
                                                                "1.0",
                                                                "classfier",
                                                                "extension");
        // repositoryFileSystem
    }


    @Test(expected = LowercaseValidationException.class)
    public void validateGroupIdCase()
            throws Exception
    {
        doReturn(repositoryFileAttributes).when(groupIdCaseValidator).getAttributes(any());
        when(repositoryFileAttributes.getCoordinates()).thenReturn(mavenArtifactCoordinates);
        groupIdCaseValidator.validateCase(repositoryPath);
    }

}
