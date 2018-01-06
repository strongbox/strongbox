package org.carlspring.strongbox.storage.metadata;

import org.carlspring.maven.commons.util.ArtifactUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.*;
import org.apache.maven.project.artifact.PluginArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class MetadataMerger
{

    private static final Logger logger = LoggerFactory.getLogger(MetadataMerger.class);

    public Metadata updateMetadataAtVersionLevel(Artifact artifact,
                                                 Metadata metadata)
    {
        if (metadata == null)
        {
            metadata = new Metadata();
            metadata.setGroupId(artifact.getGroupId());
            metadata.setArtifactId(artifact.getArtifactId());
            String newVersion = artifact.getVersion().substring(0, artifact.getVersion().indexOf("-") + 1).concat(
                    "SNAPSHOT");
            metadata.setVersion(newVersion);
        }
        // I generate timestamp once for all the merging
        String timestamp = MetadataHelper.getDateFormatInstance().format(Calendar.getInstance().getTime());

        // Update metadata o fill it for first time in case I have just created it
        Versioning versioning = metadata.getVersioning();
        if (versioning == null)
        {
            versioning = new Versioning();
            metadata.setVersioning(versioning);
        }

        Snapshot snapshot = versioning.getSnapshot();
        if (snapshot == null)
        {
            snapshot = new Snapshot();
            versioning.setSnapshot(snapshot);
        }
        snapshot.setBuildNumber(snapshot.getBuildNumber() + 1);
        snapshot.setTimestamp(timestamp.substring(0, 7) + "." + timestamp.substring(8));

        versioning.setLastUpdated(timestamp);

        List<SnapshotVersion> snapshotVersions = versioning.getSnapshotVersions();
        for (SnapshotVersion snapshotVersion : snapshotVersions)
        {
            snapshotVersion.setUpdated(timestamp);
        }

        snapshotVersions.addAll(createNewSnapshotVersions(artifact.getVersion(), timestamp, snapshot.getBuildNumber()));
        return metadata;
    }

    public Metadata updateMetadataAtArtifactLevel(Artifact artifact,
                                                  Metadata metadata)
    {
        if (metadata == null)
        {
            metadata = new Metadata();
            metadata.setGroupId(artifact.getGroupId());
            metadata.setArtifactId(artifact.getArtifactId());
        }
        String newVersion = ArtifactUtils.isReleaseVersion(artifact.getVersion()) ? artifact.getVersion() :
                            artifact.getVersion().substring(0, artifact.getVersion().indexOf("-") + 1).concat(
                                    "SNAPSHOT");
        Versioning versioning = metadata.getVersioning();
        if (versioning == null)
        {
            versioning = new Versioning();
            metadata.setVersioning(versioning);
        }
        versioning.setLatest(newVersion);
        if (ArtifactUtils.isReleaseVersion(artifact.getVersion()))
        {
            versioning.setRelease(newVersion);
        }
        List<String> versions = versioning.getVersions();

        if (!versions.contains(newVersion))
        {
            versions.add(newVersion);
        }
        versioning.setLastUpdated(MetadataHelper.getDateFormatInstance().format(Calendar.getInstance().getTime()));
        return metadata;
    }

    /**
     * This method looks at the existing Metadata.xml plugins and compares with incoming PluginArtifact.
     * If the incoming artifact plugin is not present in the existing Metadata then it tries to add the information
     * of the incoming artifact plugin else it ignores it.
     *
     * @param artifact
     * @param metadata
     * @return
     */
    public Metadata updateMetadataAtGroupLevel(PluginArtifact artifact,
                                               Metadata metadata)
    {
        if (metadata == null)
        {
            metadata = new Metadata();
        }
        List<Plugin> plugins = metadata.getPlugins();
        boolean found = false;
        for (Plugin plugin : plugins)
        {
            if (plugin.getArtifactId().equals(artifact.getArtifactId()))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            Plugin plugin = null;
            plugin = getPluginFromArtifact(artifact);
            plugins.add(plugin);
        }
        return metadata;
    }

    /**
     * This method will read the artifact and extract the plugin.xml from the artifact archive and extract it to
     * temp folder in the same location as the artifact. It then reads plugin.xml using SAX parser and extracts information.
     * After the information is read, the temp file and it's contents (plugin.xml) are deleted.
     *
     * @param artifact
     * @return
     */
    private Plugin getPluginFromArtifact(PluginArtifact artifact)
    {
        Plugin plugin = new Plugin();
        extractPluginXmlFromArtifact(artifact);
        //Read the xml file using sax parser and set the plugin value for artifact and prefix
        HashMap<String, String> pluginMap = readPluginXmlFileAndDelete(artifact);

        plugin.setName(pluginMap.get("name"));
        plugin.setPrefix(pluginMap.get("goalPrefix"));
        plugin.setArtifactId(artifact.getArtifactId());
        return plugin;
    }

    /**
     * This method iterates over the archived artifact and calls the method extractPluginXmlFile() if the plugin.xml
     * is found for extraction
     *
     * @param artifact
     */
    private void extractPluginXmlFromArtifact(PluginArtifact artifact)
    {

        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream;
        ZipEntry zipEntry;

        try
        {
            fileInputStream = new FileInputStream(artifact.getFile().getCanonicalPath());
            zipInputStream = new ZipInputStream(fileInputStream);

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                boolean isFileExtracted = extractPluginXmlFile(zipEntry, artifact, zipInputStream);
                if (isFileExtracted)
                {
                    break;
                }
            }
        }
        catch (IOException e)
        {
            logger.error(
                    "*** Error occurred while trying to extract plugin.xml from artifact " + artifact.getArtifactId() +
                    " " + e.getMessage());
        }

    }

    /**
     * This method will extract plugin.xml from artifact archive and save it in the temp directory
     *
     * @param zipEntry
     * @param artifact
     * @param zipInputStream
     * @return
     * @throws IOException
     */
    private boolean extractPluginXmlFile(ZipEntry zipEntry,
                                         PluginArtifact artifact,
                                         ZipInputStream zipInputStream)
            throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();
        boolean fileFound = false;
        if (zipEntry.getName().endsWith("plugin.xml"))
        {
            stringBuilder.append(Paths.get(artifact.getFile().getCanonicalPath().toString()).getParent().toString())
                         .append("/temp")
                         .append(artifact.getArtifactId());
            (new File(stringBuilder.toString())).mkdirs();

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(stringBuilder.append(
                    "/plugin.xml").toString()));

            byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = zipInputStream.read(buffer)) != -1)
            {
                bos.write(buffer, 0, read);
                fileFound = true;
            }

            bos.close();
        }
        return fileFound;
    }

    /**
     * This method reads the plugin.xml under temp folder and then deletes the whole temp directory
     *
     * @param artifact
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private HashMap<String, String> readPluginXmlFileAndDelete(PluginArtifact artifact)

    {
        StringBuilder stringBuilder = new StringBuilder();
        PluginHandler handler = new PluginHandler();
        SAXParserFactory saxParserFactory;
        SAXParser saxParser;

        try
        {
            stringBuilder.append(Paths.get(artifact.getFile().getCanonicalPath()).getParent())
                         .append("/temp")
                         .append(artifact.getArtifactId());
            String tempDirPath = stringBuilder.toString();
            saxParserFactory = SAXParserFactory.newInstance();
            saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new File(tempDirPath + "//plugin.xml"), handler);
            //delete the temp directory and plugin.xml
            new File(tempDirPath + "/plugin.xml").delete();
            new File(tempDirPath).delete();
        }
        catch (IOException | SAXException | ParserConfigurationException e)
        {
            logger.error("*** Error occurred while trying to parse the plugin.xml File " + e.getMessage());

        }

        return handler.getPluginMap();
    }

    private Collection<SnapshotVersion> createNewSnapshotVersions(String version,
                                                                  String timestamp,
                                                                  int buildNumber)
    {
        Collection<SnapshotVersion> toReturn = new ArrayList<>();

        SnapshotVersion sv1 = new SnapshotVersion();
        SnapshotVersion sv2 = new SnapshotVersion();
        SnapshotVersion sv3 = new SnapshotVersion();

        toReturn.add(sv1);
        toReturn.add(sv2);
        toReturn.add(sv3);

        sv1.setClassifier("javadoc");
        sv1.setExtension("jar");
        sv1.setVersion(version.replace("SNAPSHOT",
                                       timestamp.substring(0, 7) + "." + timestamp.substring(8) + "-" + buildNumber));
        sv1.setUpdated(timestamp);

        sv2.setExtension("jar");
        sv2.setVersion(version.replace("SNAPSHOT",
                                       timestamp.substring(0, 7) + "." + timestamp.substring(8) + "-" + buildNumber));
        sv2.setUpdated(timestamp);

        sv3.setExtension("pom");
        sv3.setVersion(version.replace("SNAPSHOT",
                                       timestamp.substring(0, 7) + "." + timestamp.substring(8) + "-" + buildNumber));
        sv3.setUpdated(timestamp);

        return toReturn;
    }


}
