package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.maven.artifact.Artifact;

public class MavenArtifactGeneratorStrategy implements ArtifactGeneratorStrategy<MavenArtifactGenerator>
{

    @Override
    public Path generateArtifact(MavenArtifactGenerator artifactGenerator,
                                 String id,
                                 String version,
                                 long size,
                                 Map<String, Object> attributesMap)
        throws IOException
    {
        Object licenses = attributesMap.get("licenses");
        if (licenses instanceof LicenseConfiguration[])
        {
            artifactGenerator.setLicenses((LicenseConfiguration[]) licenses);
        }

        Path result;
        if ("maven-plugin".equals(attributesMap.get("packaging")))
        {
            String gavtc = String.format("%s:%s:maven-plugin", id, version);
            Artifact pluginArtifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
            
            try
            {
                artifactGenerator.generate(pluginArtifact);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new IOException(e);
            }
            
            result = artifactGenerator.getBasedirPath().resolve(MavenArtifactUtils.convertArtifactToPath(pluginArtifact));
        }
        else
        {
            result = artifactGenerator.generateArtifact(id, version, size);
        }

        Object classifiers = attributesMap.get("classifiers");
        if (classifiers instanceof String[])
        {
            for (String classifier : (String[]) classifiers)
            {
                String gavtc = String.format("%s:%s:jar:%s", id, version, classifier);
                Artifact artifactWithClassifier = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
                try
                {
                    artifactGenerator.generate(artifactWithClassifier);
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new IOException(e);
                }
            }
        }

        return result;
    }
    
}
