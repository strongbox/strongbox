package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;

import org.xml.sax.Attributes;

class P2RepositoryUtils
{

    private P2RepositoryUtils()
    {
    }

    static P2ArtifactCoordinates createArtifact(Attributes atts)
    {
        return new P2ArtifactCoordinates(atts.getValue(P2ArtifactCoordinates.ID),
                                         atts.getValue(P2ArtifactCoordinates.VERSION),
                                         atts.getValue(P2ArtifactCoordinates.CLASSIFIER));
    }
}
