package org.carlspring.strongbox.storage.repository;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.Storage;

import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repository")
public class Repository
{

    @XmlAttribute
    private String id;

    @XmlAttribute(name = "basedir")
    private String basedir;

    @XmlAttribute
    private String policy = RepositoryPolicyEnum.MIXED.getPolicy();

    @XmlAttribute
    private String implementation = "in-memory";

    @XmlAttribute
    private String type = RepositoryTypeEnum.HOSTED.getType();

    @XmlAttribute
    private boolean secured = false;

    @XmlAttribute(name = "trash-enabled")
    private boolean trashEnabled = false;

    @XmlAttribute(name = "allows-force-deletion")
    private boolean allowsForceDeletion = false;

    @XmlAttribute(name = "allows-redeployment")
    private boolean allowsRedeployment = false;

    /**
     * The per-repository proxy settings that override the overall global proxy settings.
     */
    @XmlElement(name = "proxy-configuration")
    private ProxyConfiguration proxyConfiguration;

    @XmlTransient
    private Storage storage;


    public Repository()
    {
    }

    public Repository(String id)
    {
        this.id = id;
    }

    public Repository(String id, boolean secured)
    {
        this.id = id;
        this.secured = secured;
    }

    public boolean containsArtifact(Artifact artifact)
    {
        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);
        final File artifactFile = new File(new File(storage.getBasedir(), getId()), artifactPath);

        return artifactFile.exists();
    }

    public boolean containsPath(String path)
            throws IOException
    {
        final File artifactFile = new File(new File(storage.getBasedir(), getId()), path).getCanonicalFile();
        return artifactFile.exists();
    }

    public String pathToArtifact(Artifact artifact)
            throws IOException
    {
        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);
        final File artifactFile = new File(new File(storage.getBasedir(), getId()), artifactPath);

        return artifactFile.getCanonicalPath();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getBasedir()
    {
        if (basedir != null)
        {
            return basedir;
        }
        else
        {
            return storage.getBasedir() + "/" + id;
        }
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
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

    public boolean allowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    public void setAllowsForceDeletion(boolean allowsForceDeletion)
    {
        this.allowsForceDeletion = allowsForceDeletion;
    }

    public boolean allowsRedeployment()
    {
        return allowsRedeployment;
    }

    public void setAllowsRedeployment(boolean allowsRedeployment)
    {
        this.allowsRedeployment = allowsRedeployment;
    }

    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration)
    {
        this.proxyConfiguration = proxyConfiguration;
    }

    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.SNAPSHOT.toString().equals(getPolicy());
    }

    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.RELEASE.toString().equals(getPolicy());
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
        return id;
    }

    public File getTrashDir()
    {
        return new File(getBasedir(), ".trash");
    }

}
