package org.carlspring.strongbox.artifact.coordinates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Bespalov
 *
 */
@XmlRootElement(name = "nugetHierarchicalArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
public class NugetHierarchicalArtifactCoordinates extends NugetArtifactCoordinates
{

    private static final String NUGET_PACKAGE_REGEXP_PATTERN = "([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+).(nupkg|nuspec|nupkg\\.sha512)";

    
    public NugetHierarchicalArtifactCoordinates()
    {
    }

    public NugetHierarchicalArtifactCoordinates(String path)
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

    @Override
    public String toPath()
    {
        String idLocal = getId();
        String versionLocal = getVersion();
        String typeLocal = getType();

        if (typeLocal.equals("nuspec"))
        {
            return String.format("%s/%s/%s.%s", idLocal, versionLocal, idLocal, typeLocal);
        }

        return String.format("%s/%s/%s.%s.%s", idLocal, versionLocal, idLocal, versionLocal, typeLocal);
    }

}
