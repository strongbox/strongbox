import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient


def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0.5");

def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");
client.deleteArtifact(artifact, "storage0", "releases-with-trash");

def artifactFile = new File(System.getProperty("strongbox.storage.booter.basedir") +
                            "/storages/storage0/releases-with-trash/" +
                            "org/carlspring/maven/test-project/1.0.5/test-project-1.0.5.jar").getAbsoluteFile();

def artifactFileInTrash = new File(System.getProperty("strongbox.storage.booter.basedir") +
                                   "/storage0/releases-with-trash/.trash/" +
                                   "org/carlspring/maven/test-project/1.0.5/test-project-1.0.5.jar").getAbsoluteFile();

def artifactMd5FileInTrash = new File(System.getProperty("strongbox.storage.booter.basedir") +
                                      "/storage0/releases-with-trash/.trash/" +
                                      "org/carlspring/maven/test-project/1.0.5/test-project-1.0.5.jar.md5").getAbsoluteFile();

def artifactSha1FileInTrash = new File(System.getProperty("strongbox.storage.booter.basedir") +
                                       "/storage0/releases-with-trash/.trash/" +
                                       "org/carlspring/maven/test-project/1.0.5/test-project-1.0.5.jar.sha1").getAbsoluteFile();

return !client.artifactExists(artifact, "storage0", "releases-with-trash") && !artifactFile.exists() &&
       artifactFileInTrash.exists() && artifactMd5FileInTrash.exists() && artifactSha1FileInTrash.exists();
