package org.carlspring.strongbox.artifact.coordinates;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import static org.junit.Assert.assertThat;

import org.carlspring.maven.commons.util.ArtifactUtils;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactCoordinatesTest
{

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForZipFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(ArtifactUtils.convertPathToArtifact("org/carlspring/properties-injector/1.7/properties-injector-1.7.zip"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("zip"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForJarFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(ArtifactUtils.convertPathToArtifact("org/carlspring/properties-injector/1.7/properties-injector-1.7.jar"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForPomFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(ArtifactUtils.convertPathToArtifact("org/carlspring/properties-injector/1.7/properties-injector-1.7.pom"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("pom"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForSha1File()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(ArtifactUtils.convertPathToArtifact("org/carlspring/properties-injector/1.7/properties-injector-1.7.jar.sha1"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("sha1"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForMd5File()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(ArtifactUtils.convertPathToArtifact("org/carlspring/properties-injector/1.7/properties-injector-1.7-javadoc.jar.md5"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("md5"));
    }

    @Test
    public void ifTheArtifactDoesNotMatchFullArtifactRepositoryLayoutCoordinatesExtensionShouldResolveToJarByDefault()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(ArtifactUtils.convertPathToArtifact("org/carlspring/properties-injector/maven-metadata.xml"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

}
