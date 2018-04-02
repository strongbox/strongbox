package org.carlspring.strongbox.nuget.file;


import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@XmlAccessorType(XmlAccessType.NONE)
public class Dependency
        implements Serializable
{

    private static Logger logger = LoggerFactory.getLogger(Dependency.class);
    private static final String PACKAGE_ID_FORMAT = "[\\w\\.\\-]+";
    private static final String DEPENDENCY_FORMAT = "^(?<pkgId>[\\w\\.\\-]+):(?<version>(\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*)|\\[((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))\\]|(?<leftBorder>[\\(\\[])(?<left>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?),(?<right>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?)(?<rightBorder>[\\)\\]]))?:?(?<frameWork>[^:]+)?$";
    @XmlAttribute(
            name = "id"
    )
    private String id;
    public VersionRange versionRange;
    public Framework framework;

    public Dependency()
    {
    }

    public static Dependency parseString(String dependencyString)
            throws NugetFormatException
    {
        Pattern pattern = Pattern.compile(
                "^(?<pkgId>[\\w\\.\\-]+):(?<version>(\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*)|\\[((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))\\]|(?<leftBorder>[\\(\\[])(?<left>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?),(?<right>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?)(?<rightBorder>[\\)\\]]))?:?(?<frameWork>[^:]+)?$");
        Matcher matcher = pattern.matcher(dependencyString);
        String id;
        if (!matcher.matches())
        {
            Pattern emptyDependencyPattern = Pattern.compile("^::[^:]+");
            id = MessageFormat.format("The dependency does not match the format for RSS NuGet: {0}", dependencyString);
            if (!emptyDependencyPattern.matcher(dependencyString).matches())
            {
                throw new NugetFormatException(id);
            }
            else
            {
                logger.warn(id);
                return null;
            }
        }
        else
        {
            Dependency dependency = new Dependency();
            id = matcher.group("pkgId");
            String versionRangeString = matcher.group("version");
            String frameWorkString = matcher.group("frameWork");
            dependency.id = id;
            dependency.versionRange = VersionRange.parse(versionRangeString);
            if (frameWorkString != null && !frameWorkString.equals(""))
            {
                dependency.framework = Framework.getByShortName(frameWorkString);
                if (dependency.framework == null)
                {
                    logger.warn("Пакет: " + id + " Не найден фреймворк " + frameWorkString);
                }
            }

            return dependency;
        }
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public VersionRange getVersionRange()
    {
        return this.versionRange;
    }

    @XmlAttribute(
            name = "version"
    )
    public String getVersionRangeString()
    {
        return this.versionRange == null ? null : this.versionRange.toString();
    }

    public void setVersionRangeString(String versionRangeString)
            throws NugetFormatException
    {
        this.versionRange = VersionRange.parse(versionRangeString);
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this.getClass() != obj.getClass())
        {
            return false;
        }
        else
        {
            Dependency other = (Dependency) obj;
            if (!Objects.equals(this.id, other.id))
            {
                return false;
            }
            else
            {
                return Objects.equals(this.versionRange, other.versionRange);
            }
        }
    }

    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.versionRange);
        return hash;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append(this.id);
        builder.append(":");
        builder.append(this.versionRange);
        if (this.framework != null)
        {
            builder.append(":");
            builder.append(this.framework.name());
        }

        return builder.toString();
    }
}
