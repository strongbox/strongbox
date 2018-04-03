package org.carlspring.strongbox.nuget.file;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringListTypeAdapter
        extends XmlAdapter<String, List<String>>
{

    private String delimeter = "\\s+";
    private boolean trimSpaces = false;

    public StringListTypeAdapter()
    {
    }

    public StringListTypeAdapter(String delimeter,
                                 boolean trimSpaces)
    {
        this.delimeter = delimeter;
        this.trimSpaces = trimSpaces;
    }

    public List<String> unmarshal(String v)
    {
        String pattern = this.trimSpaces ? "\\s*" + this.delimeter + "\\s*" : this.delimeter;
        String[] marshalledStringArr = v.split(pattern);
        List<String> result = new ArrayList();

        int marshalledStringCount = marshalledStringArr.length;

        for (int i = 0; i < marshalledStringCount; ++i)
        {
            String str = marshalledStringArr[i];
            String tag = str.trim();
            if (!tag.isEmpty())
            {
                result.add(tag);
            }
        }

        return result;
    }

    public String marshal(List<String> v)
            throws Exception
    {
        Iterator<String> iter = v.iterator();
        if (!iter.hasNext())
        {
            return "";
        }
        else
        {
            StringBuilder buffer = new StringBuilder((String) iter.next());

            while (iter.hasNext())
            {
                buffer.append(this.delimeter).append((String) iter.next());
            }

            return buffer.toString();
        }
    }
}
