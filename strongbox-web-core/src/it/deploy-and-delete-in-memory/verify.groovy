import org.apache.maven.artifact.Artifact
import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient

try
{
    def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0.4");

    def client = new ArtifactClient();
    client.setUsername("maven");
    client.setPassword("password");
    client.deleteArtifact(artifact, "storage0", "releases-in-memory");

    return !client.artifactExists(artifact, "storage0", "releases-in-memory");
}
catch( Throwable t )
{
    t.printStackTrace();
    return false;
}

return true;
