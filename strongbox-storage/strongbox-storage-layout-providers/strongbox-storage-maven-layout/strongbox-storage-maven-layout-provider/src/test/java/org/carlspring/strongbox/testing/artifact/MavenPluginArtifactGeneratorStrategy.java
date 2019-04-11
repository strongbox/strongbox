package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author sbespalov
 *
 */
public class MavenPluginArtifactGeneratorStrategy implements ArtifactGeneratorStrategy<MavenArtifactGenerator>
{

    public static final String[] CLASSIFIERS = new String[] { "javadoc",
                                                              "sources",
                                                              "source-release" };

    @Override
    public Path generateArtifact(MavenArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 int size)
        throws IOException
    {
        String gavtc = String.format("%s:%s:maven-plugin", id, version);
        Artifact pluginArtifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
        try
        {
            artifactGenerator.generate(pluginArtifact);
        }
        catch (NoSuchAlgorithmException | XmlPullParserException e)
        {
            throw new IOException(e);
        }

        return artifactGenerator.getBasedirPath().resolve(MavenArtifactUtils.convertArtifactToPath(pluginArtifact));
    }

}
