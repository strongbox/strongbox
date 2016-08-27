package org.carlspring.strongbox.artifact.coordinates;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;

/**
 * @author carlspring
 */
public class MavenArtifactCoordinates extends ArtifactCoordinates
{

    private static final String GROUPID = "groupId";

    private static final String ARTIFACTID = "artifactId";

    private static final String VERSION = "version";

    private static final String CLASSIFIER = "classifier";

    private static final String EXTENSION = "extension";


    public MavenArtifactCoordinates()
    {
        defineCoordinates(GROUPID, ARTIFACTID, VERSION, CLASSIFIER, EXTENSION);
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
                default:
                    break;
            }

            i++;
        }
    }

    public MavenArtifactCoordinates(Artifact artifact)
    {
        this();

        setCoordinate(GROUPID, artifact.getGroupId());
        setCoordinate(ARTIFACTID, artifact.getArtifactId());
        setCoordinate(VERSION, artifact.getVersion());
        setCoordinate(CLASSIFIER, artifact.getClassifier());

        if (artifact.getFile() != null)
        {
            String extension = artifact.getFile().getAbsolutePath();
            extension = extension.substring(extension.lastIndexOf("."), extension.length());

            setCoordinate(EXTENSION, extension);
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

    public String getGroupId()
    {
        return getCoordinate(GROUPID);
    }

    public void setGroupId(String groupId)
    {
        setCoordinate(GROUPID, groupId);
    }

    public String getArtifactId()
    {
        return getCoordinate(ARTIFACTID);
    }

    public void setArtifactId(String artifactId)
    {
        setCoordinate(ARTIFACTID, artifactId);
    }

    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }

    public String getClassifier()
    {
        return getCoordinate(CLASSIFIER);
    }

    public void setClassifier(String classifier)
    {
        setCoordinate(CLASSIFIER, classifier);
    }

    public String getExtension()
    {
        return getCoordinate(EXTENSION);
    }

    public void setExtension(String extension)
    {
        setCoordinate(EXTENSION, extension);
    }

}
