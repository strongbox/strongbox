package org.carlspring.repositoryunit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author mtodorov
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresArtifactResource
{

    public ArtifactResource[] artifactResources();

}
