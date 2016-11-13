package org.carlspring.strongbox.providers.layout.p2;

import org.junit.Assert;
import org.junit.Test;

public class ArtifactCollectorTest {

    @Test
    public void testEmptyGet() {
        ArtifactCollector artifactCollector = create();
        Assert.assertNull(artifactCollector.get());
    }

    @Test
    public void testAcceptNullArguments() {
        ArtifactCollector artifactCollector = create();
        artifactCollector.accept(null, null);
        Assert.assertNull(artifactCollector.get());
    }

    private ArtifactCollector create() {
        return new ArtifactCollector("someBundle/1.0.0/osgi.bundle");
    }
}
