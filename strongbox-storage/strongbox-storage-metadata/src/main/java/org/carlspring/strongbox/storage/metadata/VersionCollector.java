package org.carlspring.strongbox.storage.metadata;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.carlspring.strongbox.storage.metadata.comparators.VersionComparator;
import org.carlspring.strongbox.storage.metadata.versions.MetadataVersion;
import org.carlspring.strongbox.storage.metadata.visitors.ArtifactPomVisitor;
import org.carlspring.strongbox.storage.metadata.visitors.SnapshotDirectoryVisitor;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author stodorov
 */
public class VersionCollector
{

    private ArrayList<Plugin> plugins = new ArrayList<>();


    public VersionCollector()
    {
    }

    public List<MetadataVersion> collectVersions(Path artifactBasePath)
            throws IOException, XmlPullParserException
    {
        List<MetadataVersion> versionList = new ArrayList<>();

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

                String version = pom.getVersion();

                if(artifactIsSnapshot(pom))
                {
                    Pattern pattern = Pattern.compile("^(.*-(?i)snapshot).*$");
                    Matcher matcher = pattern.matcher(version);

                    if(matcher.find())
                    {
                        version = matcher.group(1);
                    }
                }

                if(!versionListContains(versionList, version))
                {
                    MetadataVersion metadataVersion = new MetadataVersion();
                    metadataVersion.setVersion(version);
                    metadataVersion.setCreatedDate(fileAttributes.lastModifiedTime());

                    versionList.add(metadataVersion);
                }

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

        return versionList;
    }

    /**
     * Get snapshot versioning information for every relased snapshot
     *
     * @param artifactVersionPath
     */
    public List<SnapshotVersion> collectSnapshotVersions(Path artifactVersionPath)
            throws IOException
    {

        List<SnapshotVersion> snapshotVersions = new ArrayList<>();

        SnapshotDirectoryVisitor snapshotDirectoryVisitor = new SnapshotDirectoryVisitor();

        Pattern versionExtractor = Pattern.compile("^.*-(([0-9]{8})(.([0-9]+))?(-([0-9]+))?)((-)(.*))?$");

        Files.walkFileTree(artifactVersionPath, snapshotDirectoryVisitor);

        for (Path filePath : snapshotDirectoryVisitor.getMatchingPaths())
        {
            String name = filePath.toFile().getName();
            String baseName = FilenameUtils.getBaseName(name);

            Matcher matcher = versionExtractor.matcher(baseName);

            if(matcher.find())
            {
                SnapshotVersion snapshotVersion = new SnapshotVersion();
                snapshotVersion.setVersion(matcher.group(1));
                snapshotVersion.setExtension(FilenameUtils.getExtension(name));

                if(matcher.group(9) != null)
                {
                    snapshotVersion.setClassifier(matcher.group(9));
                }

                snapshotVersions.add(snapshotVersion);
            }
        }

        return snapshotVersions;
    }

    public Versioning generateVersioning(List<MetadataVersion> versions)
    {
        Versioning versioning = new Versioning();

        if (versions.size() > 0)
        {
            // Sort versions naturally (1.1 < 1.2 < 1.3 ...)
            Collections.sort(versions, new VersionComparator());
            for (MetadataVersion version : versions)
            {
                versioning.addVersion(version.getVersion());
            }

            // Sort versions naturally but consider creation date as well so that
            // 1.1 < 1.2 < 1.4 < 1.3 (1.3 is considered latest release because it was changed recently)
            // TODO: Sort this out as part of SB-333
            //Collections.sort(versions);

            versioning.setRelease(versions.get(versions.size() - 1).getVersion());
        }

        return versioning;
    }

    public Versioning generateSnapshotVersions(List<SnapshotVersion> snapshotVersionList)
    {
        Versioning versioning = new Versioning();

        if(snapshotVersionList.size() > 0)
        {
            versioning.setSnapshotVersions(snapshotVersionList);
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

    public ArrayList<Plugin> getPlugins()
    {
        return plugins;
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

    private boolean versionListContains(List<MetadataVersion> versioningList, String version)
    {
        boolean contains = false;

        if(versioningList.size() > 0)
        {
            for(MetadataVersion metadataVersion : versioningList)
            {
                if(metadataVersion.getVersion().equals(version))
                {
                    contains = true;
                    break;
                }
            }
        }

        return contains;
    }

}
