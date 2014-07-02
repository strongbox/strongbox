import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

def r0 = client.searchLucene("releases", "g:org.carlspring.maven", "json");

return r0.indexOf("\"1.0.11\"") >= 0;
