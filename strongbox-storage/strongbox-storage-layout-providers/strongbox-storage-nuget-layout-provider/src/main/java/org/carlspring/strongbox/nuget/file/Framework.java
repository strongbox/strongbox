package org.carlspring.strongbox.nuget.file;


import java.text.MessageFormat;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Framework
{
    net10(".NETFramework1.0", new String[]{ "net10" }, new String[0]),
    net11(".NETFramework1.1", new String[]{ "net11" }, new String[]{ "net10" }),
    net20(".NETFramework2.0", new String[]{ "net20" }, new String[0]),
    net30(".NETFramework3.0", new String[]{ "net30" }, new String[]{ "net20" }),
    net35(".NETFramework3.5", new String[]{ "net35" }, new String[]{ "net30",
                                                                     "net20" }),
    net40(".NETFramework4.0", new String[]{ "net40",
                                            "net40-Client" }, new String[]{ "net35",
                                                                            "net30",
                                                                            "net20" }),
    net45(".NETFramework4.5", new String[]{ "net45",
                                            "win80" }, new String[]{ "net40",
                                                                     "net35",
                                                                     "net30",
                                                                     "net20" }),
    net451(".NETFramework4.5.1", new String[]{ "net451" }, new String[]{ "net45",
                                                                         "net40",
                                                                         "net35",
                                                                         "net30",
                                                                         "net20" }),
    net452(".NETFramework4.5.2", new String[]{ "net452" }, new String[]{ "net451",
                                                                         "net45",
                                                                         "net40",
                                                                         "net35",
                                                                         "net30",
                                                                         "net20" }),
    net46(".NETFramework4.6", new String[]{ "net46" }, new String[]{ "net452",
                                                                     "net451",
                                                                     "net45",
                                                                     "net40",
                                                                     "net35",
                                                                     "net30",
                                                                     "net20" }),
    net461(".NETFramework4.6.1", new String[]{ "net461" }, new String[]{ "net46",
                                                                         "net452",
                                                                         "net451",
                                                                         "net45",
                                                                         "net40",
                                                                         "net35",
                                                                         "net30",
                                                                         "net20" }),
    net462(".NETFramework4.6.2", new String[]{ "net462" }, new String[]{ "net461",
                                                                         "net46",
                                                                         "net452",
                                                                         "net451",
                                                                         "net45",
                                                                         "net40",
                                                                         "net35",
                                                                         "net30",
                                                                         "net20" }),
    net463(".NETFramework4.6.3", new String[]{ "net463" }, new String[]{ "net462",
                                                                         "net461",
                                                                         "net46",
                                                                         "net452",
                                                                         "net451",
                                                                         "net45",
                                                                         "net40",
                                                                         "net35",
                                                                         "net30",
                                                                         "net20" }),
    portable_net45(".NETFramework4.5 Portable", new String[]{ "portable-net45" }, new String[0]),
    winrt45("WinRT 4.5", new String[]{ "winrt45" }, new String[0]),
    sl20("SilverLight 2", new String[]{ "sl2" }, new String[0]),
    sl30("SilverLight 30", new String[]{ "sl30",
                                         "sl3" }, new String[0]),
    sl4("SilverLight 4", new String[]{ "sl4",
                                       "sl40",
                                       "sl40-wp71" }, new String[0]),
    sl5("SilverLight 5", new String[]{ "sl5",
                                       "sl50" }, new String[0]),
    wp71("WindowsPhone 7.1", new String[]{ "wp71" }, new String[0]),
    wp80("WindowsPhone 8", new String[]{ "wp80" }, new String[0]),
    nativeFile("Native", new String[]{ "native" }, new String[0]);

    private final String[] fullCompabilyStringSet;
    private volatile EnumSet<Framework> fullCopabilySet;
    private final String fullName;
    private final String[] shortNames;
    private static final Logger logger = LoggerFactory.getLogger(Framework.class);
    public static final String QUERY_STRING_DELIMETER = "\\||\\+";

    private Framework(String fullName,
                      String[] shortNames,
                      String[] compabilityFrameworks)
    {
        this.shortNames = shortNames;
        this.fullName = fullName;
        this.fullCompabilyStringSet = compabilityFrameworks;
    }

    public EnumSet<Framework> getFullCopabilySet()
    {
        if (this.fullCopabilySet == null)
        {
            synchronized (this)
            {
                if (this.fullCopabilySet == null)
                {
                    this.fullCopabilySet = EnumSet.noneOf(Framework.class);
                    this.fullCopabilySet.add(this);
                    String[] var2 = this.fullCompabilyStringSet;
                    int var3 = var2.length;

                    for (int var4 = 0; var4 < var3; ++var4)
                    {
                        String frameworkName = var2[var4];
                        this.fullCopabilySet.add(valueOf(frameworkName));
                    }
                }
            }
        }

        return this.fullCopabilySet;
    }

    public String getFullName()
    {
        return this.fullName;
    }

    public String getShortName()
    {
        return this.shortNames[0];
    }

    public static EnumSet<Framework> parse(String value)
    {
        EnumSet result;
        try
        {
            if (value != null && !value.isEmpty())
            {
                result = EnumSet.noneOf(Framework.class);
                String[] frameworkStrings = value.split("\\||\\+");
                String[] var3 = frameworkStrings;
                int var4 = frameworkStrings.length;

                for (int var5 = 0; var5 < var4; ++var5)
                {
                    String frameworkString = var3[var5];
                    Framework framework = getByShortName(frameworkString.toLowerCase());
                    if (framework == null)
                    {
                        logger.warn("Can not find framework for string : \"{}\"", new Object[]{ frameworkString });
                    }
                    else
                    {
                        result.add(framework);
                    }
                }
            }
            else
            {
                result = EnumSet.allOf(Framework.class);
            }
        }
        catch (IllegalArgumentException var8)
        {
            logger.warn(MessageFormat.format("Can not find framework for string \"{0}\" used default value", value),
                        var8);
            result = EnumSet.allOf(Framework.class);
        }

        return result;
    }

    public static Framework getByFullName(String fullName)
    {
        Framework[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3)
        {
            Framework framework = var1[var3];
            if (framework.getFullName().equalsIgnoreCase(fullName))
            {
                return framework;
            }
        }

        return null;
    }

    public static Framework getByShortName(String shortName)
    {
        Framework[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3)
        {
            Framework framework = var1[var3];
            String[] var5 = framework.shortNames;
            int var6 = var5.length;

            for (int var7 = 0; var7 < var6; ++var7)
            {
                String name = var5[var7];
                if (name.equalsIgnoreCase(shortName))
                {
                    return framework;
                }
            }
        }

        return null;
    }
}
