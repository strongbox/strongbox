import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");
client.delete("storage0", "releases-with-trash", "org/carlspring/maven/test-project");

def artifactFile = new File("target/storages/storage0/releases-with-trash/" +
                             "org/carlspring/maven/test-project").getAbsoluteFile();

def artifactFileInTrash = new File("target/storages/storage0/releases-with-trash/.trash/" +
                                   "org/carlspring/maven/test-project/1.0/test-project-1.0.jar").getAbsoluteFile();

return !client.pathExists("storage0/releases-with-trash/org/caring/maven/test-project") && !artifactFile.exists() &&
       artifactFileInTrash.exists();
