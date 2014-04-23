import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient


def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0");

def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");
client.deleteArtifact(artifact, "storage0", "releases-with-trash");

def artifactFile = new File("target/storages/storage0/releases-with-trash/" +
                            "org/carlspring/maven/test-project/1.0/test-project-1.0.jar").getAbsoluteFile();

def artifactFileInTrash = new File("target/storages/storage0/releases-with-trash/.trash/" +
                                   "org/carlspring/maven/test-project/1.0/test-project-1.0.jar").getAbsoluteFile();

return !client.artifactExists(artifact, "storage0", "releases-with-trash") && !artifactFile.exists() &&
       artifactFileInTrash.exists();
