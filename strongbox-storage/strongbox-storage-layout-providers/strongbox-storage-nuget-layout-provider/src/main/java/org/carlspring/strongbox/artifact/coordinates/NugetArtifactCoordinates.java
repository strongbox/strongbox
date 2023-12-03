package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * @author Sergey Bespalov
 *
 */
@Entity
@XmlRootElement(name = "nugetArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = NugetArtifactCoordinates.LAYOUT_NAME, alias = NugetArtifactCoordinates.LAYOUT_ALIAS)
public class NugetArtifactCoordinates
        extends AbstractArtifactCoordinates<NugetArtifactCoordinates, SemanticVersion>
{

    public static final String LAYOUT_NAME = "NuGet";

    public static final String LAYOUT_ALIAS = "nuget";
    
    public static final String ID = "id";

    public static final String VERSION = "version";

    public static final String EXTENSION = "extension";

    private static final String DEFAULT_EXTENSION = "nupkg";

    private static final String NUGET_PACKAGE_REGEXP_PATTERN = "([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+).(nupkg|nuspec|nupkg\\.sha512)";

    private static final Pattern NUGET_PACKAGE_REGEXP = Pattern.compile(NUGET_PACKAGE_REGEXP_PATTERN);


    public NugetArtifactCoordinates()
    {
        resetCoordinates(ID, VERSION, EXTENSION);
    }

    public NugetArtifactCoordinates(String id,
                                    String version)
    {
        this(id, version, DEFAULT_EXTENSION);
    }

    public NugetArtifactCoordinates(String id,
                                    String version,
                                    String type)
    {
        this();
        setId(id);
        setVersion(version);
        setType(type);
    }

    @Override
    @XmlAttribute(name="id")
    @ArtifactLayoutCoordinate
    public String getId()
    {
        return getCoordinate(ID);
    }

    @Override
    public void setId(String id)
    {
        setCoordinate(ID, id);
    }

    @Override
    @XmlAttribute(name="version")
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }

    @ArtifactLayoutCoordinate
    @XmlAttribute(name="type")
    public String getType()
    {
        return getCoordinate(EXTENSION);
    }

    public void setType(String type)
    {
        setCoordinate(EXTENSION, type);
    }
    
    public String toPath()
    {
        String idLocal = getId();
        String versionLocal = getVersion();
        String typeLocal = getType();

        if ("nuspec".equals(typeLocal))
        {
            return String.format("%s/%s/%s.%s", idLocal, versionLocal, idLocal, typeLocal);
        }

        return String.format("%s/%s/%s.%s.%s", idLocal, versionLocal, idLocal, versionLocal, typeLocal);
    }

    @Override
    public URI toResource()
    {
        return URI.create("package/" + getId() + "/" + getVersion());
    }
    
    @Override
    public SemanticVersion getNativeVersion()
    {
        String versionLocal = getVersion();
        if (versionLocal == null)
        {
            return null;
        }
        try
        {
            return SemanticVersion.parse(versionLocal);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    @Override
    public Map<String, String> dropVersion()
    {
        Map<String, String> result = getCoordinates();
        result.remove(VERSION);

        return result;
    }
    
    public static NugetArtifactCoordinates parse(String path)
    {
        Matcher matcher = NUGET_PACKAGE_REGEXP.matcher(path);

        Assert.isTrue(matcher.matches(), String.format("Illegal artifact path [%s].", path));
        
        String packageId = matcher.group(1);
        String version = matcher.group(2);
        String packageArtifactName = matcher.group(3);
        String packageArtifactType = matcher.group(4);

        Assert.isTrue(String.format("%s.%s", packageId, version).startsWith(packageArtifactName),
                      String.format("Illegal artifact path [%s].", path));

        return new NugetArtifactCoordinates(packageId, version, packageArtifactType);
    }
    
}
