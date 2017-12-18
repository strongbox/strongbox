package org.carlspring.strongbox.artifact.coordinates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.maven.artifact.Artifact;

/**
 * Coordinates:
 * - Artifact ID
 * - Version
 * - Extension
 * - Artifact sub-path
 *
 * Examples of valid artifacts:
 * - com/foo/bar/1.2.3/
 * - com/foo/bar/1.2.3/foo.gz
 * - com/foo/bar/1.2.3/bar-1.2.3.tar
 * - com/foo/bar/1.2.3/blah-1.2.3.tar.bz2
 *
 * @author carlspring
 */
@XmlRootElement(name = "artifact-version-coordinates")
@XmlAccessorType(XmlAccessType.NONE)
public class ArtifactVersionCoordinates
        extends AbstractArtifactCoordinates
{

    private static final String ARTIFACTID = "artifactId";

    private static final String VERSION = "version";

    private static final String EXTENSION = "extension";

    private String artifactId;

    private String version;

    private String extension;


    public ArtifactVersionCoordinates()
    {
        defineCoordinates(ARTIFACTID, VERSION, EXTENSION);
    }

    /*
    public RawArtifactCoordinates(String path)
    {
        this(ArtifactUtils.convertPathToArtifact(path));
    }
    */

    public ArtifactVersionCoordinates(String... coordinateValues)
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
                    setArtifactId(coordinateValue);
                    break;
                case 2:
                    setVersion(coordinateValue);
                    break;
                case 3:
                    setExtension(coordinateValue);
                    break;
                default:
                    break;
            }

            i++;
        }
    }

    public ArtifactVersionCoordinates(Artifact artifact)
    {
        this();

        setArtifactId(artifact.getArtifactId());
        setVersion(artifact.getVersion());

        if (artifact.getFile() != null)
        {
            String extension = artifact.getFile().getAbsolutePath();
            extension = extension.substring(extension.lastIndexOf('.'), extension.length());

            setExtension(extension);
        }
    }

    @Override
    public String toPath()
    {
/*
        try
        {
            return ArtifactUtils.convertArtifactToPath(toArtifact());
        }
        catch (Exception e)
        {
*/
            //e.printStackTrace();
            return getCoordinates().toString();
//        }
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
    public String toString()
    {
        return "RawArtifactCoordinates{" + "artifactId='" + artifactId + '\'' +
               ", version='" + version + '\'' + ", extension='" + extension +
               '\'' + ", as path: " + toPath() + '}';
    }
}
