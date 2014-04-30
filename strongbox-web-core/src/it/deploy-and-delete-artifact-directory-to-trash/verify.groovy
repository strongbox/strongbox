import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");
client.delete("storage0", "releases-with-trash", "org/carlspring/maven/test-project/1.0.3");

def artifactFile = new File(System.getProperty("strongbox.storage.booter.basedir") +
                            "/storage0/releases-with-trash/" +
                            "org/carlspring/maven/test-project/1.0.3").getAbsoluteFile();

def artifactFileInTrash = new File(System.getProperty("strongbox.storage.booter.basedir") +
                                   "/storage0/releases-with-trash/.trash/" +
                                   "org/carlspring/maven/test-project/1.0.3/test-project-1.0.3.jar").getAbsoluteFile();

return !client.pathExists("storage0/releases-with-trash/org/carlspring/maven/test-project/1.0.3") && !artifactFile.exists() &&
       artifactFileInTrash.exists();
