package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * @author carlspring
 */
@Entity
@XmlRootElement(name = "maven-artifact-coordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = MavenArtifactCoordinates.LAYOUT_NAME, alias = MavenArtifactCoordinates.LAYOUT_ALIAS)
public class MavenArtifactCoordinates
        extends AbstractArtifactCoordinates<MavenArtifactCoordinates, ComparableVersion>
{

    public static final String LAYOUT_NAME = "Maven 2";

    public static final String LAYOUT_ALIAS = "maven";

    private static final String GROUPID = "groupId";

    private static final String ARTIFACTID = "artifactId";

    private static final String VERSION = "version";

    private static final String CLASSIFIER = "classifier";

    private static final String EXTENSION = "extension";

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String extension = "jar";


    public MavenArtifactCoordinates()
    {
        resetCoordinates(GROUPID, ARTIFACTID, VERSION, CLASSIFIER, EXTENSION);
    }

    public MavenArtifactCoordinates(String... coordinateValues)
    {
        this();

        int i = 0;
        for (String coordinateValue : coordinateValues)
        {
            // Please, forgive the following construct...
            // (In my defense, I felt equally stupid and bad for doing it this way):
            switch (i)
            {
                case 0:
                    setGroupId(coordinateValue);
                    break;
                case 1:
                    setArtifactId(coordinateValue);
                    break;
                case 2:
                    setVersion(coordinateValue);
                    break;
                case 3:
                    setClassifier(coordinateValue);
                    break;
                case 4:
                    setExtension(coordinateValue);
                    break;
                default:
                    break;
            }

            i++;
        }

        if (extension == null)
        {
            extension = "jar";
        }

    }

    public MavenArtifactCoordinates(Artifact artifact)
    {
        setGroupId(artifact.getGroupId());
        setArtifactId(artifact.getArtifactId());
        setVersion(artifact.getVersion());
        setClassifier(artifact.getClassifier());

        String type = artifact.getType();
        if (StringUtils.isNotBlank(type))
        {
            setExtension(type);
        }
        else
        {
            setExtension("jar");
        }

    }

    @Override
    public String toPath()
    {
        return MavenArtifactUtils.convertArtifactToPath(toArtifact());
    }

    public MavenArtifact toArtifact()
    {
        return new MavenRepositoryArtifact(getGroupId(), getArtifactId(), getVersion(), getExtension(), getClassifier());
    }

    @ArtifactLayoutCoordinate
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

    @ArtifactLayoutCoordinate
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
        return String.format("%s:%s", getGroupId(), getArtifactId());
    }

    @Override
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

    @ArtifactLayoutCoordinate
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

    @ArtifactLayoutCoordinate
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

    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);
        return result;
    }

    @Override
    public String toString()
    {
        return "MavenArtifactCoordinates{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId + '\'' +
               ", version='" + version + '\'' + ", classifier='" + classifier + '\'' + ", extension='" + extension +
               '\'' + ", as path: " + toPath() + '}';
    }

}
