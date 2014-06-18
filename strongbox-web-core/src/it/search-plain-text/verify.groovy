import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient


def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0.1");

def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");
def r0 = client.search("releases", "g:org.carlspring.maven v:1.0.11 p:pom");
def r1 = client.search("releases", "g:org.carlspring.maven v:1.0.11 p:p*");
def r2 = client.search("releases", "g:org.carlspring.* v:1.*.11 p:pom");
return r0.indexOf("1.0.11") >= 0 && r1.indexOf("1.0.11") >= 0 && r2.indexOf("1.0.11") >= 0;

// hopefully stupid groovy interprets it to the end