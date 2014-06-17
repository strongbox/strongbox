import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient


def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0.1");

def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");
def r = client.search("releases", "*:*");
return r.indexOf("xxx") >= 0;

// hopefully stupid groovy interprets it to the end