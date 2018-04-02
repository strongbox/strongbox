package org.carlspring.strongbox.nuget.file;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Version
        implements Comparable<Version>, Serializable
{

    public static final String VERSION_FORMAT = "(\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*)";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*)$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private final Integer major;
    private final Integer minor;
    private final Integer build;
    private final String revision;

    private static Integer parseInt(String group)
    {
        return group != null && !group.isEmpty() ? Integer.parseInt(group) : null;
    }

    public static Version parse(String versionString)
            throws NugetFormatException
    {
        if (versionString == null)
        {
            return null;
        }
        else
        {
            Matcher matcher = VERSION_PATTERN.matcher(versionString);
            if (!matcher.find())
            {
                throw new NugetFormatException(MessageFormat.format("Invalid version format: \"{0}\".", versionString));
            }
            else
            {
                Integer major = parseInt(matcher.group(1));
                Integer minor = parseInt(matcher.group(2));
                Integer build = parseInt(matcher.group(3));
                String revision = null;
                if (!matcher.group(4).isEmpty())
                {
                    revision = matcher.group(4);
                }

                return new Version(major, minor, build, revision);
            }
        }
    }

    public static boolean isValidVersionString(String versionRangeString)
    {
        return VERSION_PATTERN.matcher(versionRangeString).matches();
    }

    public Version(Integer major,
                   Integer minor,
                   Integer build,
                   String revision)
    {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = revision;
    }

    public Integer getMajor()
    {
        return this.major;
    }

    public Integer getMinor()
    {
        return this.minor;
    }

    public Integer getBuild()
    {
        return this.build;
    }

    public String getRevision()
    {
        return this.revision;
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
            Version other = (Version) obj;
            if (!Objects.equals(this.major, other.major))
            {
                return false;
            }
            else if (!Objects.equals(this.minor, other.minor))
            {
                return false;
            }
            else
            {
                return !Objects.equals(this.build, other.build) ? false : Objects.equals(this.revision, other.revision);
            }
        }
    }

    public int hashCode()
    {
        return Objects.hash(new Object[]{ this.major,
                                          this.minor,
                                          this.build,
                                          this.revision });
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.major);
        if (this.minor != null)
        {
            buffer.append(".").append(this.minor);
            if (this.build != null)
            {
                buffer.append(".").append(this.build);
                if (this.revision != null && !this.revision.trim().isEmpty())
                {
                    if (!this.revision.startsWith("-"))
                    {
                        buffer.append(".");
                    }

                    buffer.append(this.revision);
                }
            }
        }

        return buffer.toString();
    }

    public int compareTo(Version other)
    {
        if (other == null)
        {
            return 1;
        }
        else
        {
            int majorCompare = this.compareIntegerPossibleNull(this.major, other.major);
            if (majorCompare != 0)
            {
                return majorCompare;
            }
            else
            {
                int minorCompare = this.compareIntegerPossibleNull(this.minor, other.minor);
                if (minorCompare != 0)
                {
                    return minorCompare;
                }
                else
                {
                    int buildCompare = this.compareIntegerPossibleNull(this.build, other.build);
                    return buildCompare != 0 ? buildCompare :
                           this.compareStringPossibleNull(this.revision, other.revision);
                }
            }
        }
    }

    private int compareStringPossibleNull(String str1,
                                          String str2)
    {
        if (str1 == null && str2 == null)
        {
            return 0;
        }
        else if (str1 == null)
        {
            return -1;
        }
        else if (str2 == null)
        {
            return 1;
        }
        else
        {
            boolean firstStringIsNumber = NUMBER_PATTERN.matcher(str1).matches();
            boolean secondStringIsNumber = NUMBER_PATTERN.matcher(str2).matches();
            return firstStringIsNumber & secondStringIsNumber ?
                   Integer.compare(Integer.parseInt(str1), Integer.parseInt(str2)) : str1.compareToIgnoreCase(str2);
        }
    }

    private int compareIntegerPossibleNull(Integer int1,
                                           Integer int2)
    {
        if (int1 == null && int2 == null)
        {
            return 0;
        }
        else if (int1 == null)
        {
            return -1;
        }
        else
        {
            return int2 == null ? 1 : int1.compareTo(int2);
        }
    }
}
