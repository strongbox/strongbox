package org.carlspring.repositoryunit.annotations;

/**
 * @author mtodorov
 */
public @interface ArtifactResource
{

    String repository();

    String groupId();

    String artifactId();

    String version();

    String type() default "jar";

    String classifier () default "";

    long length();

    ArtifactExistenceState state();

}
