package org.carlspring.strongbox.storage.metadata;

import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.carlspring.strongbox.storage.metadata.comparators.SnapshotVersionComparator;
import org.carlspring.strongbox.storage.metadata.comparators.VersionComparator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stodorov
 */
public class VersionCollector
{

    private Versioning versioning = new Versioning();

    private ArrayList<Plugin> plugins = new ArrayList<>();
    private ArrayList<SnapshotVersion> snapshots = new ArrayList<SnapshotVersion>();

    public VersionCollector()
    {
    }

    public void processPomFiles(List<Path> foundFiles)
            throws IOException, XmlPullParserException
    {
        // Add all versions
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

                    if(!plugins.contains(plugin))
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

        Collections.sort(versioning.getVersions(), new VersionComparator());
        Collections.sort(snapshots, new SnapshotVersionComparator());

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
