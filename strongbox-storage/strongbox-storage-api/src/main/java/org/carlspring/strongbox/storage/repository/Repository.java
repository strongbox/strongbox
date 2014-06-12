package org.carlspring.strongbox.storage.repository;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.Storage;

import java.io.File;
import java.io.IOException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
public class Repository
{

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String policy = RepositoryPolicyEnum.MIXED.getPolicy();

    @XStreamAsAttribute
    private String implementation = "in-memory";

    @XStreamAsAttribute
    private String type = RepositoryTypeEnum.HOSTED.getType();

    @XStreamAsAttribute
    private boolean secured = false;

    @XStreamAlias("trash-enabled")
    @XStreamAsAttribute
    private boolean trashEnabled = false;

    @XStreamAlias("allows-redeployment")
    @XStreamAsAttribute
    private boolean allowsRedeployment = false;

    @XStreamOmitField
    private Storage storage;


    public Repository()
    {
    }

    public Repository(String name)
    {
        this.name = name;
    }

    public Repository(String name, boolean secured)
    {
        this.name = name;
        this.secured = secured;
    }

    public boolean containsArtifact(Artifact artifact)
    {
        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);
        final File artifactFile = new File(new File(storage.getBasedir(), getName()), artifactPath);

        return artifactFile.exists();
    }

    public boolean containsPath(String path)
            throws IOException
    {
        final File artifactFile = new File(new File(storage.getBasedir(), getName()), path).getCanonicalFile();
        return artifactFile.exists();
    }

    public String pathToArtifact(Artifact artifact)
            throws IOException
    {
        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);
        final File artifactFile = new File(new File(storage.getBasedir(), getName()), artifactPath);

        return artifactFile.getCanonicalPath();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPolicy()
    {
        return policy;
    }

    public void setPolicy(String policy)
    {
        this.policy = policy;
    }

    public String getImplementation()
    {
        return implementation;
    }

    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isSecured()
    {
        return secured;
    }

    public void setSecured(boolean secured)
    {
        this.secured = secured;
    }

    public boolean isTrashEnabled()
    {
        return trashEnabled;
    }

    public void setTrashEnabled(boolean trashEnabled)
    {
        this.trashEnabled = trashEnabled;
    }

    public boolean allowsRedeployment()
    {
        return allowsRedeployment;
    }

    public void setAllowsRedeployment(boolean allowsRedeployment)
    {
        this.allowsRedeployment = allowsRedeployment;
    }

    public boolean acceptsSnapshots()
    {
        return getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.toString());
    }

    public boolean acceptsReleases()
    {
        return getPolicy().equals(RepositoryPolicyEnum.RELEASE.toString());
    }

    public Storage getStorage()
    {
        return storage;
    }

    public void setStorage(Storage storage)
    {
        this.storage = storage;
    }

    @Override
    public String toString()
    {
        return name;
    }

}
