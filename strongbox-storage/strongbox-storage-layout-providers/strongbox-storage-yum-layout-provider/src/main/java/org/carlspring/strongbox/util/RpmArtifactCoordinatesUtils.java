package org.carlspring.strongbox.util;

import org.apache.commons.io.FilenameUtils;
import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.domain.RpmPackageArch;
import org.carlspring.strongbox.domain.RpmPackageType;

import javax.validation.constraints.NotEmpty;


import java.util.regex.Matcher;

import static org.carlspring.strongbox.domain.RpmNamingPatterns.*;

/**
 * Util class for parsing generating artifact coordinates by provided RPM-packages.
 * @author Ilya Shatalov <ilya@alov.me>
 */
public class RpmArtifactCoordinatesUtils
{

    /**
     * The method parse RPM-package by provided path to file.
     * The type of package depends on package arch thar will be parsed.
     *
     * For example: libglvnd-1.1.0-4.gitf92208b.fc30.i686.rpm  -  where:
     * - libglvnd          - package base name;
     * - 1.1.0             - package version;
     * - 4.gitf92208b.fc30 - package release;
     * - i686              - package architecture;
     * - rpm               - package extension.
     * Package has I686 arch, so package type will be stamped as BINARY.
     *
     * @param path to RPM package on the filesystem;
     * @return compiled RpmArtifactCoordinates;
     */
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
        String baseName;
        Matcher matcher = RPM_PACKAGE_NAME_REGEXP_PATTERN.matcher(fileName);

        if (matcher.find())
        {
            baseName = matcher.group(1);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect filename: package name is required");
        }
        return baseName;
    }

    public static String parseVersion(String fileName)
    {
        String version;
        Matcher matcher = RPM_PACKAGE_VERSION_REGEXP_PATTERN.matcher(fileName);

        if (matcher.find())
        {
            version = matcher.group(1);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect filename: package version is required");
        }
        return version;
    }

    public static String parseRelease(String fileName)
    {
        String release;
        Matcher matcher =  RPM_PACKAGE_RELEASE_REGEXP_PATTERN.matcher(fileName);

        if (matcher.find())
        {
            release = matcher.group(1);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect filename: package release is required");
        }
        return release;
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
        String arch;
        Matcher matcher = RPM_PACKAGE_ARCH_REGEXP_PATTERN.matcher(fileName);

        if (matcher.find())
        {
            arch = matcher.group(1);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect filename: package should have architecture or SRC suffix");
        }
        return RpmPackageArch.valueOf(arch.toUpperCase());
    }
}
