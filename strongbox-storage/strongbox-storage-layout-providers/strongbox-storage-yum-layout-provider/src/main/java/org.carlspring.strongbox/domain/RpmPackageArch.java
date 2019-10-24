package org.carlspring.strongbox.domain;

public enum RpmPackageArch
{
    NOARCH("noarch"),
    I386("i386"),
    X86_64("x86_64"),
    ALPHA("alpha"),
    SPARC("sparc"),
    MIPS("mips"),
    PCC("pcc"),
    PPC("ppc"),
    M68K("m68k"),
    SGI("sgi");

    private String name;

    RpmPackageArch(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
