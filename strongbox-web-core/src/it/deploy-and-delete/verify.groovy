import org.apache.maven.artifact.Artifact
import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient

try
{
    Artifact artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0");

    ArtifactClient client = new ArtifactClient();
    client.deleteArtifact(artifact, "storage0", "releases");

    File artifactFile = new File("target/storages/storage0/releases/" +
                                 "org/carlspring/maven/test-project/1.0/test-project-1.0.jar").getAbsoluteFile();

    return !client.artifactExists(artifact, "storage0", "releases") && !artifactFile.exists();
}
catch( Throwable t )
{
    System.out.println(" *[ Check failed ]* ");
    t.printStackTrace();
    return false;
}

return true;
