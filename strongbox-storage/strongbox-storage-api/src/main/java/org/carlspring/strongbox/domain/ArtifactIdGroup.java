package org.carlspring.strongbox.domain;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactIdGroup
        extends ArtifactGroup
{

    private String artifactId;

    @Override
    public void setName(String name)
    {
        artifactId = name;
    }

    @Override
    public String getName()
    {
        return artifactId;
    }


}
