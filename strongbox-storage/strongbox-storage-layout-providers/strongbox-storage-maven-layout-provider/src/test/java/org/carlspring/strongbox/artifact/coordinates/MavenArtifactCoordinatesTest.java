package org.carlspring.strongbox.artifact.coordinates;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import static org.junit.Assert.assertThat;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactCoordinatesTest
{

    private static final String GROUPID = "groupId";
    private static final String ARTIFACTID = "artifactId";
    private static final String VERSION = "version";
    private static final String CLASSIFIER = "classifier";
    private static final String EXTENSION = "extension";

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForZipFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates("org/carlspring/properties-injector/1.7/properties-injector-1.7.zip");
        assertThat(o.getExtension(), CoreMatchers.equalTo("zip"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForJarFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates("org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForPomFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates("org/carlspring/properties-injector/1.7/properties-injector-1.7.pom");
        assertThat(o.getExtension(), CoreMatchers.equalTo("pom"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForSha1File()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates("org/carlspring/properties-injector/1.7/properties-injector-1.7.jar.sha1");
        assertThat(o.getExtension(), CoreMatchers.equalTo("sha1"));
        assertThat(o.getGroupId(), CoreMatchers.equalTo("org.carlspring"));
        assertThat(o.getArtifactId(), CoreMatchers.equalTo("properties-injector"));
        assertThat(o.getClassifier(), CoreMatchers.equalTo("jar"));
        assertThat(o.getVersion(), CoreMatchers.equalTo("1.7"));
        assertThat(o.getCoordinate("extension"), CoreMatchers.equalTo("sha1"));
        assertThat(o.getCoordinate("groupId"), CoreMatchers.equalTo("org.carlspring"));
        assertThat(o.getCoordinate("artifactId"), CoreMatchers.equalTo("properties-injector"));
        assertThat(o.getCoordinate("classifier"), CoreMatchers.equalTo("jar"));
        assertThat(o.getCoordinate("version"), CoreMatchers.equalTo("1.7"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForMd5File()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates("org/carlspring/properties-injector/1.7/properties-injector-1.7-javadoc.jar.md5");
        assertThat(o.getExtension(), CoreMatchers.equalTo("md5"));
    }

    @Test
    public void ifTheArtifactDoesNotMatchFullArtifactRepositoryLayoutCoordinatesExtensionShouldResolveToJarByDefault()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates("org/carlspring/properties-injector/maven-metadata.xml");
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

}
