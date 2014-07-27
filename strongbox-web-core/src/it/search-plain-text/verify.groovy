import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

def r0 = client.search("releases", "g:org.carlspring.maven v:1.0.11 p:pom", "text");
def r1 = client.search("releases", "g:org.carlspring.maven v:1.0.11 p:p*", "text");
def r2 = client.search("releases", "g:org.carlspring.*", "text");
def r3 = client.search("releases", "g:org.carlspring.* v:1.0.1*   ", "text");
def r4 = client.search("releases", "u:org.carlspring.*|1.0.11*", "text");
def r5 = client.search("u:org.carlspring.*", "text");

System.out.println("*** g:org.carlspring.maven v:1.0.11 p:pom ***");
System.out.println(r0);
System.out.println("*********************************************");

System.out.println("*** g:org.carlspring.maven v:1.0.11 p:p* ****");
System.out.println(r1);
System.out.println("*********************************************");

System.out.println("*** g:org.carlspring.* **********************");
System.out.println(r2);
System.out.println("*********************************************");

System.out.println("*** g:org.carlspring.* **********************");
System.out.println(r5);
System.out.println("*********************************************");

return r0.indexOf("1.0.11") >= 0 && r1.indexOf("1.0.11") >= 0 &&
       r2.indexOf("1.0.11") >= 0 && r3.indexOf("1.0.5") < 0 &&
       r4.indexOf("1.0.11.1") >= 0 && r5.contains("1.0.11.1");
