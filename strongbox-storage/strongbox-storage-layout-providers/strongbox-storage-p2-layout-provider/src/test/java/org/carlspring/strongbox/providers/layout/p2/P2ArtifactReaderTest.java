package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class P2ArtifactReaderTest
{

    private static final String CLASSIFIER = "osgi.bundle";
    private static final String ID = "com.carlspring.bundle";
    private static final String VERSION = "1.0.1";
    private static final String PATH = String.format("%s/%s/%s", ID, VERSION, CLASSIFIER);

    @Test
    public void testGetArtifact()
            throws URISyntaxException, IOException
    {
        P2ArtifactCoordinates foundArtifact = getArtifact();

        assertThat(foundArtifact.getClassifier()).isEqualTo(CLASSIFIER);
        assertThat(foundArtifact.getId()).isEqualTo(ID);
        assertThat(foundArtifact.getVersion()).isEqualTo(VERSION);
    }

    @Test
    public void testNullPath()
            throws IOException
    {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(() -> assertThat(P2ArtifactReader.getArtifact(".", null)).isNull());
    }

    @Test
    public void testNullBaseDir()
            throws IOException
    {

        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(() -> assertThat(P2ArtifactReader.getArtifact(null, "")).isNull());
    }

    @Test
    public void testNullParameters()
            throws IOException
    {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(() -> assertThat(P2ArtifactReader.getArtifact(null, null)).isNull());
    }

    @Test
    public void testInvalidPath()
            throws IOException
    {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy( () -> {
                    assertThat(P2ArtifactReader.getArtifact(".", "some/invalid@path")).isNull();
                    assertThat(P2ArtifactReader.getArtifact(".", "somePath")).isNull();
                    assertThat(P2ArtifactReader.getArtifact(".", "")).isNull();
                    assertThat(P2ArtifactReader.getArtifact(".", "osgi.bundle/missingName/1.0.1")).isNull();
        });
    }

    @Test
    public void testInvalidBaseDir()
            throws IOException
    {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy( () -> assertThat(P2ArtifactReader.getArtifact("inavlidRoot!", "somePath")).isNull());
    }

    @Test
    public void testEmpryParameters()
            throws IOException
    {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy( () -> assertThat(P2ArtifactReader.getArtifact("", "")).isNull());
    }

    @Test
    public void testArtifactProperties()
            throws URISyntaxException, IOException
    {
        P2ArtifactCoordinates foundArtifact = getArtifact();
        Map<String, String> properties = foundArtifact.getProperties();
        assertThat(properties).isNotNull();

        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("id", ID);
        expectedProperties.put("version", VERSION);
        expectedProperties.put("classifier", CLASSIFIER);
        expectedProperties.put("download.size", "1");
        expectedProperties.forEach(
                (key, value) ->
                {
                    assertThat(properties.containsKey(key)).as(key + " not found").isTrue();
                    assertThat(value).isEqualTo(properties.get(key));
                });
    }

    @Test
    public void testMapping()
            throws URISyntaxException, IOException
    {
        final String repoDir = getRepoDir();
        P2ArtifactCoordinates foundArtifact = getArtifact(repoDir);
        final String expectedFilename = String.format("%s/plugins/%s_%s.jar", repoDir, ID, VERSION);
        assertThat(foundArtifact.getFilename()).isEqualTo(expectedFilename);
    }

    private P2ArtifactCoordinates getArtifact()
            throws URISyntaxException, IOException
    {
        return getArtifact(getRepoDir());
    }

    private String getRepoDir()
            throws URISyntaxException
    {
        return new File(getClass().getResource("artifacts.xml").toURI()).getParent();
    }

    private P2ArtifactCoordinates getArtifact(String repoDir)
            throws IOException
    {
        return P2ArtifactReader.getArtifact(repoDir, PATH);
    }
}
