package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;

import java.io.File;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.project.artifact.PluginArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.maven.artifact.Artifact.SNAPSHOT_VERSION;
import static org.apache.maven.artifact.Artifact.VERSION_FILE_PATTERN;

/**
 * @author Przemyslaw Fusik
 * <p>
 * Taken from org.carlspring.maven.commons.util.ArtifactUtils#getArtifactFromGAVTC
 */
public class MavenArtifactTestUtils
{

    private final static Logger logger = LoggerFactory.getLogger(MavenArtifactTestUtils.class);

    /**
     * Taken from org.carlspring.maven.commons.util.ArtifactUtils#getArtifactFromGAVTC
     */
    public static MavenArtifact getArtifactFromGAVTC(String gavtc)
    {
        logger.debug("Parsing string coordinates: {}", gavtc);

        String[] gavComponents = gavtc.split(":");

        String groupId = gavComponents[0];
        String artifactId = gavComponents[1];
        String version = gavComponents.length >= 3 ? gavComponents[2] : null;
        String type = gavComponents.length < 4 ? "jar" : gavComponents[3];
        String classifier = gavComponents.length < 5 ? null : gavComponents[4];
        String baseVersion = version;

        if (version != null)
        {
            Matcher m = VERSION_FILE_PATTERN.matcher(version);
            if (m.matches())
            {
                baseVersion = m.group(1) + "-" + SNAPSHOT_VERSION;
            }
            else
            {
                // corner case: testArtifactToPathWithClassifierAndTimestampedSnapshot
                int snapshotIndex = StringUtils.indexOfIgnoreCase(version, "-snapshot");
                if (snapshotIndex > -1 && !StringUtils.endsWithIgnoreCase(version, "-snapshot"))
                {
                    String baseVersionWithoutSnapshot = version.substring(0, snapshotIndex);
                    baseVersion = baseVersionWithoutSnapshot + "-SNAPSHOT";

                    String snapshotTimestamp = version.substring(snapshotIndex + 10);

                    if (!StringUtils.isBlank(snapshotTimestamp))
                    {
                        version = baseVersion + "-" + snapshotTimestamp;
                    }
                }
            }
        }

        Gav gav = new Gav(groupId, artifactId, version, classifier, type, null, null, null, false, null, false, null);
        MavenArtifact artifact = new TestMavenRepositoryArtifact(gav);
        artifact.setBaseVersion(baseVersion);

        logger.debug(
                "Coordinates: {} [groupId: {}; artifactId: {}, version: {}, baseVersion: {}, classifier: {}, type: {}]",
                artifact, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                artifact.getBaseVersion(), artifact.getClassifier(), artifact.getType());

        return artifact;
    }

    /**
     * Taken from org.carlspring.maven.commons.util.ArtifactUtils#getArtifactFileName
     */
    public static String getArtifactFileName(Artifact artifact)
    {
        String path = "";

        path += artifact.getArtifactId() + "-";
        path += artifact.getVersion();
        path += artifact.getClassifier() != null &&
                !artifact.getClassifier().equals("") &&
                !artifact.getClassifier().equals("null") ?
                "-" + artifact.getClassifier() : "";
        if (artifact instanceof PluginArtifact)
        {
            path += ".jar";
        }
        else
        {
            path += artifact.getType() != null ? "." + artifact.getType() : ".jar";
        }

        return path;
    }

    public static Artifact getPOMArtifact(Artifact artifact)
    {
        Gav gav = new Gav(artifact.getGroupId(),
                          artifact.getArtifactId(),
                          artifact.getVersion(),
                          artifact.getClassifier(),
                          "pom",
                          null,
                          null,
                          null,
                          false,
                          null,
                          false,
                          null);
        return new MavenRepositoryArtifact(gav);
    }

    /**
     * Taken from org.carlspring.maven.commons.util.ArtifactUtils#getGroupLevelMetadataPath
     */
    public static String getGroupLevelMetadataPath(Artifact artifact)
    {
        String artifactPath = MavenArtifactUtils.convertArtifactToPath(artifact);
        // com/foo/bar/1.2.3/bar-1.2.3.jar --> com/foo/

        String path = artifactPath.substring(0, artifactPath.lastIndexOf('/'));
        path = path.substring(0, path.lastIndexOf('/'));
        path = path.substring(0, path.lastIndexOf('/'));

        return path + "/maven-metadata.xml";
    }

    /**
     * Taken from org.carlspring.maven.commons.util.ArtifactUtils#getArtifactLevelMetadataPath
     */
    public static String getArtifactLevelMetadataPath(Artifact artifact)
    {
        String artifactPath = MavenArtifactUtils.convertArtifactToPath(artifact);
        // com/foo/bar/1.2.3/bar-1.2.3.jar --> com/foo/bar/

        String path = artifactPath.substring(0, artifactPath.lastIndexOf('/'));
        path = path.substring(0, path.lastIndexOf('/'));

        return path + "/maven-metadata.xml";
    }

    /**
     * Taken from org.carlspring.maven.commons.util.ArtifactUtils#getVersionLevelMetadataPath
     */
    public static String getVersionLevelMetadataPath(Artifact artifact)
    {
        String artifactPath = MavenArtifactUtils.convertArtifactToPath(artifact);
        // com/foo/bar/1.2.3-SNAPSHOT/bar-1.2.3-SNAPSHOT.jar --> com/foo/bar/1.2.3-SNAPSHOT

        String path = artifactPath.substring(0, artifactPath.lastIndexOf('/'));

        return path + "/maven-metadata.xml";
    }

    private static class TestMavenRepositoryArtifact
            extends MavenRepositoryArtifact
    {

        private File file;

        public TestMavenRepositoryArtifact(Gav gav)
        {
            super(gav);
        }

        @Override
        public void setFile(File destination)
        {
            this.file = destination;
        }

        @Override
        public File getFile()
        {
            return file;
        }
    }
}
