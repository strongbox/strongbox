import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

def r0 = client.search("g:org.carlspring.maven", "xml");

System.out.println(r0);

return r0.indexOf(">1.0.11.3<") >= 0 && r0.indexOf(">1.0.11.3.1<") >= 0;

