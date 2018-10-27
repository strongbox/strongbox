package org.carlspring.strongbox.storage.metadata;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.project.artifact.PluginArtifact;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MetadataHelperTest
{

    private static final Object POM = "pom";

    private static final String JAR = "jar";

    private static final String VERSION = "1.1";

    private static final String PRE_VERSION = "1.0";

    private static final String JAVADOC = "javadoc";

    private static final String ANOTHER_ARTIFACT_ID = "mockito-all";

    private static final String ARTIFACT_ID = "strongbox-maven-metadata-api";

    private static final String SNAPSHOT_VERSION = "1.0-SNAPSHOT";

    private static final String GROUP_ID = "org.carlspring.strongbox";

    private MetadataMerger metadataMerger;

    private File pluginXmlFile;
    private static String pluginXmlFilePath;
    @Mock
    private Artifact artifact;

    @Mock
    private PluginArtifact pluginArtifact;


    @Before
    public void setUp()
    {
        initMocks(this);
        metadataMerger = new MetadataMerger();
    }

    @BeforeClass
    public static void setUpBeforeAll()
            throws IOException
    {
        createPluginXmlFile();
        crateJarFile();
    }

    @AfterClass
    public static void down()
    {
        deleteTestResources();

    }

    private static void deleteTestResources()
    {
        Path dirPath = Paths.get(pluginXmlFilePath).getParent();
        try
        {
            Files.walk(dirPath)
                 .map(Path::toFile)
                 .sorted(Comparator.comparing(File::isDirectory))
                 .forEach(File::delete);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void crateJarFile()
    {
        String parentPluginPath = String.valueOf(Paths.get(pluginXmlFilePath).getParent().getParent().getParent());
        try (FileOutputStream fos = new FileOutputStream(parentPluginPath + "/maven-dependency-plugin-3.0.2.jar");
             ZipOutputStream zipOS = new ZipOutputStream(fos))
        {
            writeToZipFile(pluginXmlFilePath + "/plugin.xml", zipOS);
            System.out.println("");

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeToZipFile(String path,
                                       ZipOutputStream zipStream)
            throws IOException
    {
        File aFile = new File(path);
        FileInputStream fis = new FileInputStream(aFile);
        ZipEntry zipEntry = new ZipEntry(path);
        zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0)
        {
            zipStream.write(bytes, 0, length);
        }

        zipStream.closeEntry();
        fis.close();

    }

    private static void createPluginXmlFile()
            throws IOException
    {
        File file = new File("");
        pluginXmlFilePath = file.getCanonicalPath().toString() + "/src/test/resources/maven-dependency-plugin-3.0.2/META-INF/maven";
        Files.createDirectories(Paths.get(pluginXmlFilePath));

        try
        {
            String xmlSource = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                               "<plugin>\n" +
                               "  <name>Apache Maven Dependency Plugin</name>\n" +
                               "  <description>Provides utility goals to work with dependencies like copying, unpacking, analyzing, resolving and many more.</description>\n" +
                               "  <groupId>org.apache.maven.plugins</groupId>\n" +
                               "  <artifactId>maven-dependency-plugin</artifactId>\n" +
                               "  <version>3.0.2</version>\n" +
                               "  <goalPrefix>dependency</goalPrefix>\n" +
                               "</plugin>";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));

            // Write the parsed document to an xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(pluginXmlFilePath + "/plugin.xml"));
            transformer.transform(source, result);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (TransformerConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void versionLevelCreateNewMetadaTest()
    {

        // Given
        when(artifact.getGroupId()).thenReturn(GROUP_ID);
        when(artifact.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(artifact.getVersion()).thenReturn(SNAPSHOT_VERSION);

        // When
        Metadata metadata = metadataMerger.updateMetadataAtVersionLevel(artifact, null);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(SNAPSHOT_VERSION, metadata.getVersion());

        Assert.assertNotNull(metadata.getVersioning());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot());
        Assert.assertEquals(1, metadata.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot().getTimestamp());
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
        Assert.assertEquals(3, metadata.getVersioning().getSnapshotVersions().size());

        Assert.assertEquals(JAVADOC, metadata.getVersioning().getSnapshotVersions().get(0).getClassifier());
        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(0).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(0).getUpdated());

        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(1).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(1).getUpdated());

        Assert.assertEquals(POM, metadata.getVersioning().getSnapshotVersions().get(2).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(2).getUpdated());

        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(0).getVersion()
                                  .equals(metadata.getVersioning().getSnapshotVersions().get(1).getVersion()));
        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(1).getVersion()
                                  .equals(metadata.getVersioning().getSnapshotVersions().get(2).getVersion()));
    }

    @Test
    public void versionLevelUpdateExistingMetadataTest()
    {
        // Given
        Metadata metadata = createVersionLevelMetadata();
        when(artifact.getVersion()).thenReturn(SNAPSHOT_VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtVersionLevel(artifact, metadata);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(SNAPSHOT_VERSION, metadata.getVersion());

        Assert.assertNotNull(metadata.getVersioning());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot());
        Assert.assertEquals(2, metadata.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot().getTimestamp());
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
        Assert.assertEquals(6, metadata.getVersioning().getSnapshotVersions().size());

        Assert.assertEquals(JAVADOC, metadata.getVersioning().getSnapshotVersions().get(3).getClassifier());
        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(3).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(3).getUpdated());

        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(4).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(4).getUpdated());

        Assert.assertEquals(POM, metadata.getVersioning().getSnapshotVersions().get(5).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(5).getUpdated());

        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(3).getVersion()
                                  .equals(metadata.getVersioning().getSnapshotVersions().get(4).getVersion()));
        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(4).getVersion()
                                  .equals(metadata.getVersioning().getSnapshotVersions().get(5).getVersion()));
    }

    @Test
    public void artifactLevelCreateNewMetadataTest()
    {
        // Given
        when(artifact.getGroupId()).thenReturn(GROUP_ID);
        when(artifact.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(artifact.getVersion()).thenReturn(VERSION);

        // When
        Metadata metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, null);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());

        Assert.assertNotNull(metadata.getVersioning());
        Assert.assertEquals(VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(VERSION, metadata.getVersioning().getRelease());
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());

        Assert.assertEquals(1, metadata.getVersioning().getVersions().size());
        Assert.assertEquals(VERSION, metadata.getVersioning().getVersions().get(0));
    }

    @Test
    public void artifactLevelUpdateMetadataTest()
    {
        // Given
        Metadata metadata = createArtifactLevelMetadata();
        when(artifact.getVersion()).thenReturn(VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, metadata);

        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(VERSION, metadata.getVersioning().getRelease());
        Assert.assertEquals(2, metadata.getVersioning().getVersions().size());
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(VERSION));
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(PRE_VERSION));
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
    }

    @Test
    public void artifactLevelUpdateMetadataVersionExistsTest()
    {
        // Given
        Metadata metadata = createArtifactLevelMetadata();
        when(artifact.getVersion()).thenReturn(PRE_VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, metadata);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(PRE_VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(PRE_VERSION, metadata.getVersioning().getRelease());
        Assert.assertEquals(1, metadata.getVersioning().getVersions().size());
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(PRE_VERSION));
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());

    }

    @Test
    public void artifactLevelUpdateMetadataSnapshotTest()
    {
        // Given
        Metadata metadata = createArtifactLevelMetadata();
        when(artifact.getVersion()).thenReturn(SNAPSHOT_VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, metadata);

        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(SNAPSHOT_VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(PRE_VERSION, metadata.getVersioning().getRelease());
        Assert.assertEquals(2, metadata.getVersioning().getVersions().size());
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(SNAPSHOT_VERSION));
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(PRE_VERSION));
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
    }

    @Test
    public void groupLevelCreateMetadataTest()
    {
        // Given
        when(pluginArtifact.getGroupId()).thenReturn(GROUP_ID);
        when(pluginArtifact.getArtifactId()).thenReturn("maven-dependency-plugin");

        String filePath = Paths.get(pluginXmlFilePath).getParent().getParent().getParent().toString() + "/maven-dependency-plugin-3.0.2.jar";
        when(pluginArtifact.getFile()).thenReturn(new File(filePath));

        // When
        Metadata metadata = metadataMerger.updateMetadataAtGroupLevel((PluginArtifact) pluginArtifact, null);

        // Then
        Assert.assertNotNull(metadata.getPlugins());
        Assert.assertEquals(1, metadata.getPlugins().size());
        Assert.assertEquals("maven-dependency-plugin", metadata.getPlugins().get(0).getArtifactId());
        Assert.assertEquals("Apache Maven Dependency Plugin", metadata.getPlugins().get(0).getName());
        Assert.assertEquals("dependency", metadata.getPlugins().get(0).getPrefix());
    }

    @Test
    public void groupLevelUpdateMetadataAddPluginTest()
            throws IOException
    {
        // Given
        Metadata metadata = createGroupLevelMetadata();
        when(pluginArtifact.getArtifactId()).thenReturn("maven-dependency-plugin");
        String filePath = Paths.get(pluginXmlFilePath).getParent().getParent().getParent().toString() + "/maven-dependency-plugin-3.0.2.jar";
        when(pluginArtifact.getFile()).thenReturn(new File(filePath));

        // When
        metadata = metadataMerger.updateMetadataAtGroupLevel(pluginArtifact, metadata);

        // Then
        Assert.assertEquals(2, metadata.getPlugins().size());
        Assert.assertEquals("maven-dependency-plugin", metadata.getPlugins().get(1).getArtifactId());
        Assert.assertEquals("Apache Maven Dependency Plugin", metadata.getPlugins().get(1).getName());
        Assert.assertEquals("dependency", metadata.getPlugins().get(1).getPrefix());
    }

    @Test
    public void groupLevelUpdateMetadataPluginExistsTest()
    {
        // Given
        Metadata metadata = createGroupLevelMetadata();
        when(pluginArtifact.getArtifactId()).thenReturn(ANOTHER_ARTIFACT_ID);

        // When
        metadata = metadataMerger.updateMetadataAtGroupLevel(pluginArtifact, metadata);

        // Then
        Assert.assertEquals(1, metadata.getPlugins().size());
        Assert.assertEquals(ANOTHER_ARTIFACT_ID, metadata.getPlugins().get(0).getArtifactId());
        Assert.assertEquals("", metadata.getPlugins().get(0).getName());
        Assert.assertEquals("", metadata.getPlugins().get(0).getPrefix());
    }

    private Metadata createGroupLevelMetadata()
    {
        Metadata metadata = new Metadata();

        Plugin plugin = new Plugin();
        plugin.setArtifactId(ANOTHER_ARTIFACT_ID);
        plugin.setName("");
        plugin.setPrefix("");

        List<Plugin> plugins = new ArrayList<>();
        plugins.add(plugin);
        metadata.setPlugins(plugins);
        return metadata;
    }

    private Metadata createArtifactLevelMetadata()
    {
        Metadata metadata = new Metadata();

        metadata.setGroupId(GROUP_ID);
        metadata.setArtifactId(ARTIFACT_ID);
        metadata.setVersioning(new Versioning());
        metadata.getVersioning().setLatest(PRE_VERSION);
        metadata.getVersioning().setRelease(PRE_VERSION);
        List<String> versions = new ArrayList<>();
        versions.add(PRE_VERSION);
        metadata.getVersioning().setVersions(versions);
        metadata.getVersioning()
                .setLastUpdated(MetadataHelper.getDateFormatInstance().format(Calendar.getInstance().getTime()));
        return metadata;
    }

    private Metadata createVersionLevelMetadata()
    {
        Metadata metadata = new Metadata();
        metadata.setGroupId(GROUP_ID);
        metadata.setArtifactId(ARTIFACT_ID);
        metadata.setVersion(SNAPSHOT_VERSION);

        String timestamp = MetadataHelper.getDateFormatInstance().format(Calendar.getInstance().getTime());

        Snapshot snapshot = new Snapshot();
        snapshot.setBuildNumber(1);
        snapshot.setTimestamp(timestamp.substring(0, 7) + "." + timestamp.substring(8));

        List<SnapshotVersion> snapshotVersions = new ArrayList<>();
        snapshotVersions.addAll(createNewSnapshotVersions(SNAPSHOT_VERSION, timestamp, 1));

        Versioning versioning = new Versioning();
        versioning.setSnapshot(snapshot);
        versioning.setLastUpdated(timestamp);
        versioning.setSnapshotVersions(snapshotVersions);

        metadata.setVersioning(versioning);
        return metadata;
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
