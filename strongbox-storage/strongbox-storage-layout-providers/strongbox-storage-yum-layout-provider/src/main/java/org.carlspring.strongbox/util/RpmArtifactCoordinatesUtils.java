package org.carlspring.strongbox.util;

import org.apache.commons.io.FilenameUtils;
import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.domain.RpmPackageArch;
import org.carlspring.strongbox.domain.RpmPackageType;

import javax.validation.constraints.NotEmpty;

import java.util.regex.Matcher;

import static org.carlspring.strongbox.domain.RpmNamingPatterns.*;

public class RpmArtifactCoordinatesUtils
{
    public static RpmArtifactCoordinates parse(@NotEmpty String path)
    {
        String fileName = FilenameUtils.getName(path);
        if (!fileName.endsWith(".rpm"))
        {
            throw new IllegalArgumentException("The artifact packaging can be only '.rpm'");
        }

        String baseName = parseBaseName(fileName);
        String version = parseVersion(fileName);
        String release = parseRelease(fileName);
        RpmPackageType packageType = parsePackageType(fileName);


        RpmArtifactCoordinates artifactCoordinates;
        if (packageType == RpmPackageType.SOURCE)
        {
            artifactCoordinates = new RpmArtifactCoordinates(baseName, version, release, packageType);
        }
        else
        {
            RpmPackageArch arch = parseArch(fileName);
            artifactCoordinates = new RpmArtifactCoordinates(baseName, version, release, packageType, arch);
        }

        return artifactCoordinates;
    }

    public static String parseBaseName(String fileName)
    {
        return RPM_PACKAGE_NAME_REGEXP_PATTERN.matcher(fileName).group(1);
    }

    public static String parseVersion(String fileName)
    {
        return RPM_PACKAGE_VERSION_REGEXP_PATTERN.matcher(fileName).group(1);
    }

    public static String parseRelease(String fileName)
    {
        return RPM_PACKAGE_RELEASE_REGEXP_PATTERN.matcher(fileName).group(1);
    }

    public static RpmPackageType parsePackageType (String fileName)
    {
        Boolean match = RPM_PACKAGE_TYPE_REGEXP_PATTERN.matcher(fileName).find();

        return match
                ? RpmPackageType.SOURCE
                : RpmPackageType.BINARY;
    }


    public static RpmPackageArch parseArch (String fileName)
    {
        String arch = RPM_PACKAGE_ARCH_REGEXP_PATTERN.matcher(fileName).group(1);
        return RpmPackageArch.valueOf(arch.toUpperCase());
    }
}
