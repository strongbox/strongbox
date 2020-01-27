package org.carlspring.strongbox.artifact.coordinates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.M2GavCalculator;
import org.carlspring.strongbox.domain.LayoutArtifactCoordinatesEntity;

/**
 * @author carlspring
 */
@XmlRootElement(name = "maven-artifact-coordinates")
@XmlAccessorType(XmlAccessType.NONE)
public class MockedMavenArtifactCoordinates
        extends LayoutArtifactCoordinatesEntity<MockedMavenArtifactCoordinates, ComparableVersion>
{

    private static final M2GavCalculator M2_GAV_CALCULATOR = new M2GavCalculator();

    private static final String GROUPID = "groupId";

    private static final String ARTIFACTID = "artifactId";

    private static final String VERSION = "version";

    private static final String CLASSIFIER = "classifier";

    private static final String EXTENSION = "extension";

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String extension;


    public MockedMavenArtifactCoordinates()
    {
        resetCoordinates(GROUPID, ARTIFACTID, VERSION, CLASSIFIER, EXTENSION);
    }

    public MockedMavenArtifactCoordinates(Artifact artifact)
    {
        this();

        setGroupId(artifact.getGroupId());
        setArtifactId(artifact.getArtifactId());
        setVersion(artifact.getVersion());
        setClassifier(artifact.getClassifier());

        if (artifact.getFile() != null)
        {
            String extension = artifact.getFile()
                                       .getAbsolutePath();
            extension = extension.substring(extension.lastIndexOf('.'));

            setExtension(extension);
        }
        else
        {
            setExtension("jar");
        }
    }

    @Override
    public String convertToPath(MockedMavenArtifactCoordinates c)
    {
        try
        {
            return convertArtifactToPath(c.toArtifact());
        }
        catch (Exception e)
        {
            return getCoordinates().toString();
        }
    }

    public Artifact toArtifact()
    {
        return new DefaultArtifact(getGroupId(),
                                   getArtifactId(),
                                   getVersion(),
                                   "compile",
                                   getExtension(),
                                   getClassifier(),
                                   new DefaultArtifactHandler(getExtension()));
    }

    @XmlAttribute(name = "groupId")
    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
        setCoordinate(GROUPID, this.groupId);
    }

    @XmlAttribute(name = "artifactId")
    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
        setCoordinate(ARTIFACTID, this.artifactId);
    }

    @Override
    public String getId()
    {
        return artifactId;
    }

    public void setId(String id)
    {
        setArtifactId(id);
    }

    @Override
    @XmlAttribute(name = "version")
    public String getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(String version)
    {
        this.version = version;
        setCoordinate(VERSION, this.version);
    }

    @XmlAttribute(name = "classifier")
    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
        setCoordinate(CLASSIFIER, this.classifier);
    }

    @XmlAttribute(name = "extension")
    public String getExtension()
    {
        return extension;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
        setCoordinate(EXTENSION, this.extension);
    }

    @Override
    public ComparableVersion getNativeVersion()
    {
        String versionLocal = getVersion();
        if (versionLocal == null)
        {
            return null;
        }
        return new ComparableVersion(versionLocal);
    }

    private static String convertArtifactToPath(Artifact artifact)
    {
        final Gav gav = new Gav(artifact.getGroupId(),
                                StringUtils.defaultString(artifact.getArtifactId()),
                                StringUtils.defaultString(artifact.getVersion()),
                                artifact.getClassifier(), artifact.getType(), null, null, null, false, null, false,
                                null);
        return M2_GAV_CALCULATOR.gavToPath(gav).substring(1);
    }
}
