package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.domain.RpmPackageArch;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class is an {@link ArtifactCoordinates} implementation for RPM-packages.
 *
 * The canonical named package is represented to below structure:
 * {name}-{version}-{release}.{architecture}.{classifier}.rpm
 * Examples: somepackage-1.0-1.x86_64.rpm
 * @author Ilya Shatalov
 */
@Entity
@SuppressWarnings("serial")
@XmlRootElement(name = "PypiArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = YumArtifactCoordinates.LAYOUT_NAME, alias = YumArtifactCoordinates.LAYOUT_ALIAS)
public class YumArtifactCoordinates
        extends AbstractArtifactCoordinates<YumArtifactCoordinates, SemanticVersion>
{
    public static final String LAYOUT_NAME = "Yum";

    public static final String LAYOUT_ALIAS = "yum";

    public static final String DISTRIBUTION = "distribution";

    public static final String VERSION = "version";

    public static final String RELEASE = "release";

    public static final String ARCHITECTURE = "architecture";

    private static final String RPM_PACKAGE_NAME_REGEXP = "^([a-zA-Z0-9-]*)(?=-\\d\\.)";
    private static final String RPM_PACKAGE_VERSION_REGEXP = "(?<=-)([\\d\\.]+)(?=-)";
    private static final String RPM_PACKAGE_RELEASE_REGEXP = "(?<=\\d-)([\\d\\.a-z]+)(?=\\.)";
    private static final String RPM_PACKAGE_ARCH_REGEXP = "(i386|alpha|sparc|mips|ppc|pcc|m68k|SGI|x86_64|noarch);l(?=(\\.rpm$))";
    private static final String RPM_PACKAGE_EXTENSION_REGEXP = "(\\.rpm)(?=$)";

    private static final Pattern RPM_PACKAGE_NAME_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_NAME_REGEXP);
    private static final Pattern RPM_PACKAGE_VERSION_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_VERSION_REGEXP);
    private static final Pattern RPM_PACKAGE_RELEASE_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_RELEASE_REGEXP);
    private static final Pattern RPM_PACKAGE_ARCH_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_ARCH_REGEXP);
    private static final Pattern RPM_PACKAGE_EXT_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_EXTENSION_REGEXP);

    @Override
    public String getId()
    {
        return DISTRIBUTION;
    }

    @Override
    public void setId(String id)
    {
        setCoordinate(DISTRIBUTION, id);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        setCoordinate(VERSION, version);
    }

    public String getRelease()
    {
        return getCoordinate(RELEASE);
    }

    public void setRelease(String release)
    {
        setCoordinate(RELEASE, release);
    }

    public String getArchitecture()
    {
        return getCoordinate(ARCHITECTURE);
    }

    public void setArchitecture(RpmPackageArch arch)
    {
        setCoordinate(ARCHITECTURE, arch.getName());
    }

    @Override
    public SemanticVersion getNativeVersion()
    {
        String version = getVersion();

        return version == null || version.isEmpty()
                ? null
                : SemanticVersion.parse(version);
    }

    @Override
    public Map<String, String> dropVersion()
    {
        return null;
    }

    @Override
    public String toPath()
    {
        return null;
    }
}
