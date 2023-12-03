package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

/**
 * @author carlspring
 */
public interface DependencySynonymFormatter
{

    void register();

    String getLayout();

    String getFormatAlias();

    String getDependencySnippet(ArtifactCoordinates coordinates);

}
