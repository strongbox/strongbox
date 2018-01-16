package org.carlspring.strongbox.artifact.coordinates;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.semver.Version;

/**
 * @author Sergey Bespalov
 *
 */

@XmlRootElement(name = "nugetArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
public class NugetArtifactCoordinates extends AbstractArtifactCoordinates<NugetArtifactCoordinates, Version>
{
    public static final String ID = "Id";
    public static final String VERSION = "Version";
    public static final String EXTENSION = "Extension";
    private static final String NUGET_PACKAGE_REGEXP_PATTERN = "([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+).(nupkg|nuspec|nupkg\\.sha512)";


    public NugetArtifactCoordinates()
    {
        defineCoordinates(ID, VERSION, EXTENSION);
    }

    public NugetArtifactCoordinates(String path)
    {
        Pattern pattern = Pattern.compile(NUGET_PACKAGE_REGEXP_PATTERN);
        Matcher matcher = pattern.matcher(path);
        if (!matcher.matches())
        {
            return;
        }

        String packageId = matcher.group(1);
        String version = matcher.group(2);
        String packageArtifactName = matcher.group(3);
        String packageArtifactType = matcher.group(4);

        if (!String.format("%s.%s", packageId, version).startsWith(packageArtifactName))
        {
            return;
        }

        setId(packageId);
        setVersion(version);
        setType(packageArtifactType);
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
    public Version getNativeVersion()
    {
        String versionLocal = getVersion();
        if (versionLocal == null)
        {
            return null;
        }
        try
        {
            return Version.parse(versionLocal);
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
    
}
