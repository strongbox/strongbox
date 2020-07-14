package org.carlspring.strongbox.domain;

import java.util.EnumSet;

public enum RpmPackageArch
{

    NOARCH("noarch"),
    I386("i386"),
    I686("i686"),
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

    public static RpmPackageArch fromValue(String name)
    {
        return valueOf(name.toUpperCase());
    }

    public String getName()
    {
        return name;
    }

}
