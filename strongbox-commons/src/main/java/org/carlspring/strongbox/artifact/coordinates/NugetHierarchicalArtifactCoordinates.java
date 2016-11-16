package org.carlspring.strongbox.artifact.coordinates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Bespalov
 *
 */
public class NugetHierarchicalArtifactCoordinates extends NugetArtifactCoordinates
{

    private static final String NUGET_PACKAGE_REGEXP_PATTERN = "([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+).nupkg";

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
        String packageArtifactName = String.format("%s.%s.nupkg",
                                                   packageId,
                                                   version);
        if (!packageArtifactName.startsWith(matcher.group(3)))
        {
            return;
        }
        setId(packageId);
        setVersion(version);
    }

    @Override
    public String toPath()
    {
        String idLocal = getId();
        String versionLocal = getVersion();
        return String.format("%s/%s/%s.%s.nupkg",
                             idLocal,
                             versionLocal,
                             idLocal,
                             versionLocal);
    }

}
