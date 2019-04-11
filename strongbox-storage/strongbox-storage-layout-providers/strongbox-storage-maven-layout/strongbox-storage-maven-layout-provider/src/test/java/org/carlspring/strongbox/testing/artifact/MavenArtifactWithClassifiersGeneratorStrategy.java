package org.carlspring.strongbox.testing.artifact;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author sbespalov
 *
 */
public class MavenArtifactWithClassifiersGeneratorStrategy implements ArtifactGeneratorStrategy<MavenArtifactGenerator>
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
        Path result = artifactGenerator.generateArtifact(id, version, size);

        for (String classifier : CLASSIFIERS)
        {
            String gavtc = String.format("%s:%s:jar:%s", id, version, classifier);
            Artifact artifactWithClassifier = ArtifactUtils.getArtifactFromGAVTC(gavtc);
            try
            {
                artifactGenerator.generate(artifactWithClassifier);
            }
            catch (NoSuchAlgorithmException | XmlPullParserException e)
            {
                throw new IOException(e);
            }
        }

        return result;
    }

}
