package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * This class is an {@link ArtifactCoordinates} implementation for Composer
 * artifacts. <br>
 * See <a href="https://getcomposer.org/doc/04-schema.md#name">Official Composer
 * documentation</a>.
 *
 * @author jcoelho93
 */
@Entity
@SuppressWarnings("serial")
@XmlRootElement(name = "composerArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = ComposerArtifactCoordinates.LAYOUT_NAME, alias = ComposerArtifactCoordinates.LAYOUT_ALIAS)
public class ComposerArtifactCoordinates
        extends AbstractArtifactCoordinates<ComposerArtifactCoordinates, SemanticVersion>
{


    public static final String LAYOUT_NAME = "composer";

    public static final String LAYOUT_ALIAS = LAYOUT_NAME;

    public static final String COMPOSER_VERSION_REGEX = "^(v)?(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(-(dev|patch|p|alpha|a|beta|b|RC))?([1-9]\\d*)?";

    private static final Pattern COMPOSER_VERSION_PATTERN = Pattern.compile(COMPOSER_VERSION_REGEX);

    private static final String VENDOR = "vendor";

    private static final String NAME = "name";

    private static final String VERSION = "version";

    private static final String TYPE = "type";

    private String vendor;

    private String name;

    private String version;

    private String type;

    public ComposerArtifactCoordinates(String vendor,
                                       String name)
    {
        setVendor(vendor);
        setName(name);
    }

    public ComposerArtifactCoordinates(String vendor,
                                       String name,
                                       String version,
                                       String type)
    {
        setVendor(vendor);
        setName(name);
        setVersion(version);
        setType(type);
    }

    @Override
    public String getId()
    {
        return String.format("%s/%s", getVendor(), getName());
    }

    @Override
    public void setId(String id)
    {
        setName(id);
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(String version)
    {
        Matcher matcher = COMPOSER_VERSION_PATTERN.matcher(version);
        Assert.isTrue(matcher.matches(),
                      String.format("The artifact's version [%s] should follow the Composer specification " +
                                    "(https://getcomposer.org/doc/04-schema.md#version).",
                                    version));

        this.version = version;
        setCoordinate(VERSION, this.version);
    }

    @Override
    public SemanticVersion getNativeVersion()
    {
        try
        {
            return SemanticVersion.parse(this.version);
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

    @Override
    public String toPath()
    {
        String path = String.format("%s/%s", getVendor(), getName());

        if (getVersion() != null)
        {
            path += "/" + getVersion();
        }

        if (getType() != null)
        {
            path += "/" + getType();
        }

        return path;
    }

    @ArtifactLayoutCoordinate
    @XmlAttribute(name = VENDOR)
    public String getVendor()
    {
        return vendor;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
        setCoordinate(VENDOR, this.vendor);
    }

    @ArtifactLayoutCoordinate
    @XmlAttribute(name = NAME)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        setCoordinate(NAME, name);
    }

    public static ComposerArtifactCoordinates parse(String path)
    {
        String[] coordinates = path.split("/");

        if (coordinates.length < 2 || coordinates[0].isEmpty() || coordinates[1].isEmpty())
        {
            throw new IllegalArgumentException("Invalid Composer package path");
        }

        if (coordinates.length == 2)
        {
            return new ComposerArtifactCoordinates(coordinates[0], coordinates[1]);
        }
        else if (coordinates.length == 3)
        {
            try
            {
                String version = SemanticVersion.parse(coordinates[2]).toString();
                return new ComposerArtifactCoordinates(coordinates[0], coordinates[1], version, null);
            }
            catch (IllegalArgumentException e)
            {
                ComposerArtifactCoordinates cac = new ComposerArtifactCoordinates(coordinates[0], coordinates[1]);
                cac.setType(coordinates[2]);
                return cac;
            }
        }
        else
        {
            return new ComposerArtifactCoordinates(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        }
    }

    @ArtifactLayoutCoordinate
    @XmlAttribute(name = TYPE)
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

}
