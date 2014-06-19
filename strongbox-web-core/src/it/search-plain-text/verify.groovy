import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

def r0 = client.search("releases", "g:org.carlspring.maven v:1.0.11 p:pom");
def r1 = client.search("releases", "g:org.carlspring.maven v:1.0.11 p:p*");
def r2 = client.search("releases", "g:org.carlspring.*");

System.out.println("*** g:org.carlspring.maven v:1.0.11 p:pom ***");
System.out.println(r0);
System.out.println("*********************************************");

System.out.println("*** g:org.carlspring.maven v:1.0.11 p:p* ****");
System.out.println(r1);
System.out.println("*********************************************");

System.out.println("*** g:org.carlspring.* **********************");
System.out.println(r2);
System.out.println("*********************************************");

return r0.indexOf("1.0.11") >= 0 && r1.indexOf("1.0.11") >= 0 && r2.indexOf("1.0.11") >= 0;
