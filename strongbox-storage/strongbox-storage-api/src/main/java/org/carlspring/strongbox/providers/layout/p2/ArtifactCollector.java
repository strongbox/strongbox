package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.xml.sax.Attributes;

class ArtifactCollector implements P2Collector<P2ArtifactCoordinates> {

    private final P2ArtifactCoordinates bundleToFind;

    private P2ArtifactCoordinates artifact;

    public ArtifactCollector(String bundleToFind) {
        this.bundleToFind = P2ArtifactCoordinates.create(bundleToFind);
    }

    @Override
    public P2ArtifactCoordinates get() {
        return artifact;
    }

    @Override
    public void accept(String localName, Attributes atts) {
        if ("artifact".equals(localName)) {
            P2ArtifactCoordinates artifact = P2RepositoryUtils.createArtifact(atts);
            if (artifact.equals(bundleToFind)) {
                this.artifact = artifact;
            }
        }
    }
}
