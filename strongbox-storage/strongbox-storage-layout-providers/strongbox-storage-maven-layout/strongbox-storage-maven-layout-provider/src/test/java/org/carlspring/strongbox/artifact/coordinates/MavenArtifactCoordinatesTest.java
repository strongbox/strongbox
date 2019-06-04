package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@Execution(CONCURRENT)
public class MavenArtifactCoordinatesTest
{

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForZipFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(
                MavenArtifactUtils.convertPathToArtifact(
                        "org/carlspring/properties-injector/1.7/properties-injector-1.7.zip"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("zip"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForJarFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(MavenArtifactUtils.convertPathToArtifact(
                "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForPomFile()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(MavenArtifactUtils.convertPathToArtifact(
                "org/carlspring/properties-injector/1.7/properties-injector-1.7.pom"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("pom"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForSha1File()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(MavenArtifactUtils.convertPathToArtifact(
                "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar.sha1"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

    @Test
    public void mavenArtifactCoordinatesShouldReturnProperExtensionForMd5File()
    {
        MavenArtifactCoordinates o = new MavenArtifactCoordinates(MavenArtifactUtils.convertPathToArtifact(
                "org/carlspring/properties-injector/1.7/properties-injector-1.7-javadoc.jar.md5"));
        assertThat(o.getExtension(), CoreMatchers.equalTo("jar"));
    }

    @Test
    public void mavenMetadataShouldNotBeResolvedAsArtifact()
    {
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(
                "org/carlspring/properties-injector/maven-metadata.xml");
        assertThat(artifact, CoreMatchers.nullValue());
    }

}
