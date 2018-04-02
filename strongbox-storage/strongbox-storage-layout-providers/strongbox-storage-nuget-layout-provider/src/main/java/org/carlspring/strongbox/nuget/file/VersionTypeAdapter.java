package org.carlspring.strongbox.nuget.file;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class VersionTypeAdapter
        extends XmlAdapter<String, Version>
{

    public VersionTypeAdapter()
    {
    }

    public String marshal(Version version)
            throws Exception
    {
        return version == null ? null : version.toString();
    }

    public Version unmarshal(String string)
            throws Exception
    {
        return string == null ? null : Version.parse(string);
    }
}
