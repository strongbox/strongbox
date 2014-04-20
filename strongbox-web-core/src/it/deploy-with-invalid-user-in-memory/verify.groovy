import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient


def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0-SNAPSHOT");

def client = new ArtifactClient();

// This should throw a ResponseException as the user is not valid.
def response = client.artifactExistsStatusCode(artifact, "storage0", "snapshots-in-memory");

return response.getStatus() == 401;
