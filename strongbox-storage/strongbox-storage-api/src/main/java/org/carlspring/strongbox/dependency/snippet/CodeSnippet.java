package org.carlspring.strongbox.dependency.snippet;

import javax.annotation.Nonnull;

public class CodeSnippet
    implements Comparable<CodeSnippet>
{
    
    protected String name;
    
    protected String code;
    
    
    public CodeSnippet(String name, String code)
    {
        this.name = name;
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public int compareTo(@Nonnull CodeSnippet codeSnippet)
    {
        return name.compareTo(codeSnippet.getName());
    }

}
