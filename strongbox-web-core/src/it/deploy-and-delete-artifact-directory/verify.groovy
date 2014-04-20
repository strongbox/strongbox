import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

client.delete("storage0", "releases", "org/carlspring/maven/test-project");

def artifactFile = new File("target/storages/storage0/releases/" +
                             "org/carlspring/maven/test-project").getAbsoluteFile();

return !client.pathExists("storage0/releases/org/caring/maven/test-project") && !artifactFile.exists();
