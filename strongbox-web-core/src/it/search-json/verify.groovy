import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

def r0 = client.searchLuceneRepo("releases", "g:org.carlspring.maven", "json", "na");

System.out.println(r0);

return r0.indexOf("\"1.0.11.2.1\"") >= 0;
