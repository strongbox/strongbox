import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

client.delete("storage0", "releases", "org/carlspring/maven/test-project/1.0.2");

def artifactFile = new File("target/storages/storage0/releases/" +
                             "org/carlspring/maven/test-project/1.0.2").getAbsoluteFile();

return !client.pathExists("storage0/releases/org/carlspring/maven/test-project/1.0.2") && !artifactFile.exists();
