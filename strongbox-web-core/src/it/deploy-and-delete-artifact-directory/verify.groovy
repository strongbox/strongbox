import org.carlspring.strongbox.client.ArtifactClient

try
{
    ArtifactClient client = new ArtifactClient();
    client.delete("storage0", "releases", "org/carlspring/maven/test-project");

    File artifactFile = new File("target/storages/storage0/releases/" +
                                 "org/carlspring/maven/test-project").getAbsoluteFile();

    return !client.pathExists("storage0/releases/org/carlspring/maven/test-project") && !artifactFile.exists();
}
catch( Throwable t )
{
    System.out.println(" *[ Check failed ]* ");
    t.printStackTrace();
    return false;
}

return true;
