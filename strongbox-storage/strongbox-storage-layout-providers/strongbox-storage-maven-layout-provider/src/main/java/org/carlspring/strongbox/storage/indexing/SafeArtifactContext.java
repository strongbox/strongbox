package org.carlspring.strongbox.storage.indexing;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.model.Model;

/**
 * @author Przemyslaw Fusik
 */
public class SafeArtifactContext
        extends ArtifactContext
{

    public SafeArtifactContext(ArtifactContext ac)
    {
        super(ac.getPom(), ac.getArtifact(), ac.getMetadata(), ac.getArtifactInfo(), ac.getGav());
    }

    @Override
    public Model getPomModel()
    {
        Model pomModel = super.getPomModel();

        if (pomModel == null && getArtifactInfo() != null && getArtifact() != null && !getArtifact().isFile())
        {
            pomModel = new Model();
            pomModel.setGroupId(getArtifactInfo().getGroupId());
            pomModel.setArtifactId(getArtifactInfo().getArtifactId());
            pomModel.setVersion(getArtifactInfo().getVersion());
            pomModel.setPackaging(getArtifactInfo().getPackaging());
        }
        return pomModel;
    }
}
