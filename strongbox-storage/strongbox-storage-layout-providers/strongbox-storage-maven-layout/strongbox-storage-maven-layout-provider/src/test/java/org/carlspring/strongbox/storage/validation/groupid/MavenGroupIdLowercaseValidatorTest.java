package org.carlspring.strongbox.storage.validation.groupid;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.storage.validation.artifact.LowercaseValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


/**
 * Created by dinesh on 12/7/17.
 */
@Execution(CONCURRENT)
public class MavenGroupIdLowercaseValidatorTest
{

    @Spy
    MavenGroupIdLowercaseValidator groupIdCaseValidator = new MavenGroupIdLowercaseValidator();

    MavenArtifactCoordinates mavenArtifactCoordinates;

    @Mock
    RepositoryFileAttributes repositoryFileAttributes;
    
    @BeforeEach
    public void setUp()
    {
        initMocks(this);
        mavenArtifactCoordinates = new MavenArtifactCoordinates("org.dinesh.artifact.is.Uppercase",
                                                                "my-maven-artifact",
                                                                "1.0",
                                                                "classfier",
                                                                "extension");
    }


    @Test
    public void validateGroupIdCase()
            throws Exception
    {
        doReturn(repositoryFileAttributes).when(groupIdCaseValidator).getAttributes(any());
        when(repositoryFileAttributes.getCoordinates()).thenReturn(mavenArtifactCoordinates);

        assertThrows(LowercaseValidationException.class, () -> {
            groupIdCaseValidator.validate(null, mavenArtifactCoordinates);
        });
    }

}
