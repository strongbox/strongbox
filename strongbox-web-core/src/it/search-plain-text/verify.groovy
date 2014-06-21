import org.carlspring.strongbox.client.ArtifactClient


def client = new ArtifactClient();
client.setUsername("maven");
client.setPassword("password");

def r0 = client.searchLucene("releases", "g:org.carlspring.maven v:1.0.11 p:pom");
def r1 = client.searchLucene("releases", "g:org.carlspring.maven v:1.0.11 p:p*");
def r2 = client.searchLucene("releases", "g:org.carlspring.*");
def r3 = client.searchLucene("releases", "g:org.carlspring.* v:1.0.1*   ");
def r4 = client.searchLucene("releases", "u:org.carlspring.*|1.0.12*");
def r5 = client.searchLucene("u:org.carlspring.*");

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
       r4.indexOf("1.0.12") >= 0 && r5.contains("1.0.12");
