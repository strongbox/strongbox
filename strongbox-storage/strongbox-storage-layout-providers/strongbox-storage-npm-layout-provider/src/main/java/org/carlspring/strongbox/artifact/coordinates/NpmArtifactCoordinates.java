package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semver.Version;
import org.springframework.util.Assert;

/**
 * This class is an {@link ArtifactCoordinates} implementation for npm
 * artifacts. <br>
 * See <a href="https://docs.npmjs.com/files/package.json">Official npm package
 * specification</a>.
 * 
 * @author sbespalov
 *
 */
@Entity
@SuppressWarnings("serial")
@XmlRootElement(name = "npmArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactLayout("npm")
public class NpmArtifactCoordinates extends AbstractArtifactCoordinates<NpmArtifactCoordinates, Version>
{
    public static final String NPM_VERSION_REGEX = "(\\d+)\\.(\\d+)(?:\\.)?(\\d*)(\\.|-|\\+)?([0-9A-Za-z-.]*)?";
    public static final String NPM_NAME_REGEX = "[a-z0-9][\\w-.]*";
    public static final String NPM_EXTENSION_REGEX = "(tgz|json)";
    public static final String NPM_PACKAGE_PATH_REGEX = "(@?" + NPM_NAME_REGEX + ")/(" + NPM_NAME_REGEX + ")/("
            + NPM_VERSION_REGEX + ")/" + NPM_NAME_REGEX + "(-(" + NPM_VERSION_REGEX + "))?\\." + NPM_EXTENSION_REGEX;

    private static final Pattern NPM_NAME_PATTERN = Pattern.compile(NPM_NAME_REGEX);
    private static final Pattern NPM_PATH_PATTERN = Pattern.compile(NPM_PACKAGE_PATH_REGEX);
    private static final Pattern NPM_EXTENSION_PATTERN = Pattern.compile(NPM_EXTENSION_REGEX);

    private static final String SCOPE = "scope";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String EXTENSION = "extension";

    public NpmArtifactCoordinates()
    {
        resetCoordinates(SCOPE, NAME, VERSION, EXTENSION);
    }

    public NpmArtifactCoordinates(String scope,
                                  String name,
                                  String version,
                                  String extension)
    {
        setScope(scope);
        setName(name);
        setVersion(version);
        setExtension(extension);
    }

    public String getScope()
    {
        return getCoordinate(SCOPE);
    }

    public void setScope(String scope)
    {
        if (scope == null)
        {
            return;
        }
        Assert.isTrue(scope.startsWith("@"), "Scope should starts with '@'.");
        setCoordinate(SCOPE, scope);
    }

    public String getName()
    {
        return getCoordinate(NAME);
    }

    public void setName(String name)
    {
        Matcher matcher = NPM_NAME_PATTERN.matcher(name);
        Assert.isTrue(matcher.matches(),
                      "The artifact's name should follow the NPM specification  (https://docs.npmjs.com/files/package.json#name).");

        setCoordinate(NAME, name);
    }

    @Override
    public String getId()
    {
        if (getScope() == null)
        {
            return getName();
        }
        return String.format("%s/%s", getScope(), getName());
    }

    @Override
    public void setId(String id)
    {
        setName(id);
    }

    @Override
    public String getVersion()
    {
        return getCoordinate(VERSION);
    }

    @Override
    public void setVersion(String version)
    {
        Version.parse(version);
        setCoordinate(VERSION, version);
    }

    public void setExtension(String extension)
    {
        Matcher matcher = NPM_EXTENSION_PATTERN.matcher(extension);
        Assert.isTrue(matcher.matches(), "Invalid artifact extension");
        
        setCoordinate(EXTENSION, extension);
    }

    public String getExtension() {
        return getCoordinate(EXTENSION);
    }
    
    @Override
    public String toPath()
    {
        return String.format("%s/%s/%s/%s", getGroup(), getName(), getVersion(), getArtifactFileName());
    }

    @Override
    public URI toResource()
    {
        return URI.create(String.format("%s/-/%s", getId(), getArtifactFileName()));
    }

    public String getGroup()
    {
        String scopeLocal = getScope();
        String nameLocal = getName();

        return scopeLocal == null ? nameLocal : scopeLocal;
    }

    public String getArtifactFileName()
    {
        if ("json".equals(getExtension()))
        {
            return "package.json"; 
        }
        return String.format("%s-%s.%s", getName(), getVersion(), getExtension());
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

    public static NpmArtifactCoordinates parse(String path)
    {
        Matcher matcher = NPM_PATH_PATTERN.matcher(path);

        Assert.isTrue(matcher.matches(),
                      String.format("Illegal artifact path [%s], NPM artifact path should be in the form of '{artifactGroup}/{artifactName}/{artifactVersion}/{artifactFile}'.",
                                    path));

        String group = matcher.group(1);
        String name = matcher.group(2);
        String version = matcher.group(3);
        String extension = matcher.group(16);

        if (group.startsWith("@"))
        {
            return new NpmArtifactCoordinates(group, name, version, extension);
        }
        return new NpmArtifactCoordinates(null, name, version, extension);
    }

    public static NpmArtifactCoordinates of(String name,
                                            String version)
    {
        if (name.contains("/"))
        {
            String[] nameSplit = name.split("/");
            return new NpmArtifactCoordinates(nameSplit[0], nameSplit[1], version, "tgz");
        }
        return new NpmArtifactCoordinates(null, name, version, "tgz");
    }

}
