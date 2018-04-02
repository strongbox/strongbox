package org.carlspring.strongbox.nuget.file;


import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VersionRange
        implements Serializable
{

    public static final String BORDER_DELIMETER = ",";
    public static final String FULL_VERSION_RANGE_PATTERN = "(?<leftBorder>[\\(\\[])(?<left>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?),(?<right>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?)(?<rightBorder>[\\)\\]])";
    public static final String FIXED_VERSION_RANGE_PATTERN = "\\[((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))\\]";
    private Version lowVersion;
    private VersionRange.BorderType lowBorderType;
    private VersionRange.BorderType topBorderType;
    private Version topVersion;

    public boolean isLatestVersion()
    {
        return this.lowVersion == null && this.topVersion == null;
    }

    public boolean isFixedVersion()
    {
        return this.lowVersion != null && this.topVersion != null && this.lowVersion.equals(this.topVersion);
    }

    public boolean isSimpleRange()
    {
        return this.topVersion == null && this.lowVersion != null &&
               this.lowBorderType == VersionRange.BorderType.INCLUDE;
    }

    public VersionRange()
    {
    }

    public VersionRange(Version lowVersion,
                        VersionRange.BorderType lowBorderType,
                        Version topVersion,
                        VersionRange.BorderType topBorderType)
    {
        this.lowVersion = lowVersion;
        this.lowBorderType = lowBorderType;
        this.topBorderType = topBorderType;
        this.topVersion = topVersion;
    }

    public Version getLowVersion()
    {
        return this.lowVersion;
    }

    public void setLowVersion(Version lowVersion)
    {
        this.lowVersion = lowVersion;
    }

    public Version getTopVersion()
    {
        return this.topVersion;
    }

    public void setTopVersion(Version topVersion)
    {
        this.topVersion = topVersion;
    }

    public VersionRange.BorderType getLowBorderType()
    {
        return this.lowBorderType;
    }

    public void setLowBorderType(VersionRange.BorderType lowBorderType)
    {
        this.lowBorderType = lowBorderType;
    }

    public VersionRange.BorderType getTopBorderType()
    {
        return this.topBorderType;
    }

    public void setTopBorderType(VersionRange.BorderType topBorderType)
    {
        this.topBorderType = topBorderType;
    }

    public String toString()
    {
        if (this.isLatestVersion())
        {
            return "";
        }
        else if (this.isFixedVersion())
        {
            return "[" + this.topVersion.toString() + "]";
        }
        else if (this.isSimpleRange())
        {
            return this.lowVersion.toString();
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            if (this.lowVersion != null)
            {
                builder.append(this.lowBorderType.lowBorderSymbol);
                builder.append(this.lowVersion.toString());
            }
            else
            {
                builder.append(VersionRange.BorderType.EXCLUDE.lowBorderSymbol);
            }

            builder.append(",");
            if (this.topVersion != null)
            {
                builder.append(this.topVersion.toString());
                builder.append(this.topBorderType.topBorderSymbol);
            }
            else
            {
                builder.append(VersionRange.BorderType.EXCLUDE.topBorderSymbol);
            }

            return builder.toString();
        }
    }

    public static VersionRange parse(String versionRangeString)
            throws NugetFormatException
    {
        if (versionRangeString != null && !versionRangeString.isEmpty())
        {
            if (Version.isValidVersionString(versionRangeString))
            {
                Version version = Version.parse(versionRangeString);
                return new VersionRange(version, VersionRange.BorderType.INCLUDE, (Version) null,
                                        (VersionRange.BorderType) null);
            }
            else
            {
                Pattern fixedVersionPattern = Pattern.compile("^\\[((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))\\]$");
                Matcher fixedVersionMatcher = fixedVersionPattern.matcher(versionRangeString);
                if (fixedVersionMatcher.matches())
                {
                    Version version = Version.parse(fixedVersionMatcher.group(1));
                    return new VersionRange(version, VersionRange.BorderType.INCLUDE, version,
                                            VersionRange.BorderType.INCLUDE);
                }
                else
                {
                    Pattern pattern = Pattern.compile(
                            "^(?<leftBorder>[\\(\\[])(?<left>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?),(?<right>((\\d+)\\.?(\\d*)\\.?(\\d*)\\.?([-\\d\\w_]*))?)(?<rightBorder>[\\)\\]])$");
                    Matcher matcher = pattern.matcher(versionRangeString);
                    if (matcher.matches())
                    {
                        Version lowVersion = null;
                        VersionRange.BorderType lowBorder = null;
                        String lowVersionString = matcher.group("left");
                        if (!lowVersionString.isEmpty())
                        {
                            lowVersion = Version.parse(lowVersionString);
                            lowBorder = VersionRange.BorderType.getBorderType(matcher.group("leftBorder"));
                        }

                        Version topVersion = null;
                        VersionRange.BorderType topBorder = null;
                        String topVersionString = matcher.group("right");
                        if (!topVersionString.isEmpty())
                        {
                            topVersion = Version.parse(topVersionString);
                            topBorder = VersionRange.BorderType.getBorderType(matcher.group("rightBorder"));
                        }

                        return new VersionRange(lowVersion, lowBorder, topVersion, topBorder);
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        }
        else
        {
            return new VersionRange();
        }
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
            VersionRange other = (VersionRange) obj;
            if (!Objects.equals(this.lowVersion, other.lowVersion))
            {
                return false;
            }
            else if (this.lowBorderType != other.lowBorderType)
            {
                return false;
            }
            else if (this.topBorderType != other.topBorderType)
            {
                return false;
            }
            else
            {
                return Objects.equals(this.topVersion, other.topVersion);
            }
        }
    }

    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.lowVersion);
        hash = 59 * hash + (this.lowBorderType != null ? this.lowBorderType.hashCode() : 0);
        hash = 59 * hash + (this.topBorderType != null ? this.topBorderType.hashCode() : 0);
        hash = 59 * hash + Objects.hashCode(this.topVersion);
        return hash;
    }

    public static enum BorderType
    {
        INCLUDE("[", "]"),
        EXCLUDE("(", ")");

        private final String lowBorderSymbol;
        private final String topBorderSymbol;

        private static VersionRange.BorderType getBorderType(String borderSymbol)
        {
            if (borderSymbol != null && !borderSymbol.isEmpty())
            {
                if (!borderSymbol.equals(INCLUDE.lowBorderSymbol) && !borderSymbol.equals(INCLUDE.topBorderSymbol))
                {
                    return !borderSymbol.equals(EXCLUDE.lowBorderSymbol) &&
                           !borderSymbol.equals(EXCLUDE.topBorderSymbol) ? null : EXCLUDE;
                }
                else
                {
                    return INCLUDE;
                }
            }
            else
            {
                return null;
            }
        }

        private BorderType(String lowBorderSymbol,
                           String topBorderSymbol)
        {
            this.lowBorderSymbol = lowBorderSymbol;
            this.topBorderSymbol = topBorderSymbol;
        }

        public String getLowBorderSymbol()
        {
            return this.lowBorderSymbol;
        }

        public String getTopBorderSymbol()
        {
            return this.topBorderSymbol;
        }
    }
}
