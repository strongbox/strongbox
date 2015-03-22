package org.carlspring.strongbox.storage.metadata;

import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.carlspring.strongbox.storage.metadata.comparators.VersionComparator;
import org.carlspring.strongbox.storage.metadata.versions.MetadataVersion;
import org.carlspring.strongbox.storage.metadata.visitors.ArtifactPomVisitor;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stodorov
 */
public class VersionCollector
{

    private Versioning versioning = new Versioning();

    private List<MetadataVersion> releasedVersions = new ArrayList<>();

    private ArrayList<Plugin> plugins = new ArrayList<>();
    private ArrayList<SnapshotVersion> snapshots = new ArrayList<SnapshotVersion>();

    public VersionCollector()
    {
    }


    public void collectVersions(Path artifactBasePath)
            throws IOException, XmlPullParserException
    {
        ArtifactPomVisitor artifactPomVisitor = new ArtifactPomVisitor();

        // Find all artifact versions
        Files.walkFileTree(artifactBasePath, artifactPomVisitor);

        // Add all versions
        for (Path filePath : artifactPomVisitor.getMatchingPaths())
        {
            Model pom = getPom(filePath);

            // No pom, no metadata.
            if (pom != null)
            {
                BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);

                MetadataVersion metadataVersion = new MetadataVersion();
                metadataVersion.setVersion(pom.getVersion());
                metadataVersion.setCreatedDate(fileAttributes.lastModifiedTime());

                releasedVersions.add(metadataVersion);

                if(artifactIsPlugin(pom))
                {
                    String name = pom.getName() != null ?
                                  pom.getName() :
                                  pom.getArtifactId();
                    String prefix = pom.getArtifactId().replace("maven-plugin", "").replace("-plugin$", "");

                    Plugin plugin = new Plugin();
                    plugin.setName(name);
                    plugin.setArtifactId(pom.getArtifactId());
                    plugin.setPrefix(prefix);

                    if (!plugins.contains(plugin))
                    {
                        plugins.add(plugin);
                    }
                }
            }
        }
    }

    public void collectSnapshotVersions(Path artifactBasePath)
    {

    }



    public Versioning getReleasedVersioning()
    {
        Versioning versioning = new Versioning();

        if (releasedVersions.size() > 0)
        {
            // Sort versions naturally (1.1 < 1.2 < 1.3 ...)
            Collections.sort(releasedVersions, new VersionComparator());
            for (MetadataVersion version : releasedVersions)
            {
                versioning.addVersion(version.getVersion());
            }

            // Sort versions naturally but consider creation date as well so that
            // 1.1 < 1.2 < 1.4 < 1.3 (1.3 is considered latest release because it was changed recently)
            Collections.sort(releasedVersions);
            versioning.setRelease(releasedVersions.get(releasedVersions.size() - 1).getVersion());
        }

        return versioning;
    }

    public void processPomFiles(List<Path> foundFiles)
            throws IOException, XmlPullParserException
    {
/*        // Add all versions
        for (Path filePath : foundFiles)
        {
            Model pom = getPom(filePath);

            // No pom, no metadata.
            if (pom != null)
            {
                if (artifactIsSnapshot(pom))
                {
                    SnapshotVersion snapshotVersion = new SnapshotVersion();
                    snapshotVersion.setVersion(pom.getVersion());

                    // Add the snapshot version to versioning to follow the standard.
                    versioning.addVersion(pom.getVersion());

                    // Add the snapshot version to an array so we can later create necessary metadata for the snapshot
                    snapshots.add(snapshotVersion);
                }
                else if (artifactIsPlugin(pom))
                {
                    String name = pom.getName() != null ?
                                  pom.getName() :
                                  pom.getArtifactId();
                    String prefix = pom.getArtifactId().replace("maven-plugin", "").replace("-plugin$", "");

                    Plugin plugin = new Plugin();
                    plugin.setName(name);
                    plugin.setArtifactId(pom.getArtifactId());
                    plugin.setPrefix(prefix);

                    if (!plugins.contains(plugin))
                    {
                        plugins.add(plugin);
                    }

                    versioning.addVersion(pom.getVersion());
                }
                else
                {
                    versioning.addVersion(pom.getVersion());
                }

            }
        }

        // Fix sorting
        Collections.sort(versioning.getVersions(), new VersionComparator());
        Collections.sort(snapshots, new SnapshotVersionComparator());

        // Set latest version & latest release version
        if (versioning.getVersions().size() > 0)
        {
            VersionComparator versionComparator = new VersionComparator();

            String latestVersion = "0";
            String latestRelease = "0";

            for (String version : versioning.getVersions())
            {
                // Latest version include SNAPSHOT versions as well.
                if(versionComparator.compare(version, latestVersion) == 1)
                {
                    latestVersion = version;
                }

                // Latest release excludes SNAPSHOT versions
                if(!version.matches("^(.+)-((?i)snapshot).*$") && versionComparator.compare(version, latestRelease) == 1)
                {
                    latestRelease = version;
                }
            }

            versioning.setLatest(latestVersion);
            versioning.setRelease(latestRelease);
        }*/

    }

    public Versioning getVersioning()
    {
        return versioning;
    }

    public ArrayList<Plugin> getPlugins()
    {
        return plugins;
    }

    public ArrayList<SnapshotVersion> getSnapshots()
    {
        return snapshots;
    }

    private boolean artifactIsPlugin(Model model)
    {
        return model.getArtifactId().matches("^(.+)-((?i)plugin)$");
    }

    private boolean artifactIsSnapshot(Model model)
    {
        return model.getVersion().matches("^(.+)-((?i)snapshot).*$");
    }

    private Model getPom(Path filePath)
            throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(new FileReader(filePath.toFile()));
    }

}
