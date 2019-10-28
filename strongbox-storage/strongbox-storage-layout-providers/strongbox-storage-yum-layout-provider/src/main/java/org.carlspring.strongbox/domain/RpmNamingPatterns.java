package org.carlspring.strongbox.domain;

import java.util.regex.Pattern;

public class RpmNamingPatterns
{
    public static final String RPM_PACKAGE_NAME_REGEXP = "^([a-zA-Z0-9_\\-+]+?)(?=-\\d)";

    public static final String RPM_PACKAGE_VERSION_REGEXP = "(?<=-)([\\d\\.]+)(?=-)";

    public static final String RPM_PACKAGE_RELEASE_REGEXP = ".*-([a-zA-Z0-9_.\\-\\+]*)\\..*\\.rpm";

    public static final String RPM_PACKAGE_TYPE_REGEXP = "(src)(?=(\\.rpm$))";

    public static final String RPM_PACKAGE_ARCH_REGEXP = "(i386|i686|alpha|sparc|mips|ppc|pcc|m68k|SGI|x86_64|noarch)(?=(\\.rpm$))";

    public static final String RPM_PACKAGE_EXTENSION_REGEXP = "(\\.rpm)(?=$)";



    public static final Pattern RPM_PACKAGE_NAME_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_NAME_REGEXP);

    public static final Pattern RPM_PACKAGE_VERSION_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_VERSION_REGEXP);

    public static final Pattern RPM_PACKAGE_RELEASE_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_RELEASE_REGEXP);

    public static final Pattern RPM_PACKAGE_TYPE_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_TYPE_REGEXP);

    public static final Pattern RPM_PACKAGE_ARCH_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_ARCH_REGEXP);

    public static final Pattern RPM_PACKAGE_EXT_REGEXP_PATTERN = Pattern.compile(RPM_PACKAGE_EXTENSION_REGEXP);
}
