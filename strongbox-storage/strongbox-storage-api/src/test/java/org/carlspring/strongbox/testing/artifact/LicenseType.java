package org.carlspring.strongbox.testing.artifact;

/**
 * This class contains some common licenses that can be used when generating test artifacts.
 *
 * @author carlspring
 */
public enum LicenseType
{

    APACHE_2_0("Apache 2.0", "apache-2.0", "http://www.apache.org/licenses/LICENSE-2.0"),

    AGPL_3_0("GNU Affero General Public License", "agpl-3.0", "https://www.gnu.org/licenses/agpl-3.0.en.html"),

    BSL_1_0("Boost Software License 1.0", "bsl-1.0", "https://www.boost.org/users/license.html"),

    EPL_2_0("Eclipse Public License 2.0", "epl-1.0", "https://www.eclipse.org/legal/epl-2.0/"),

    GPL_3_0("GNU General Public License", "gpl-3.0", "https://www.gnu.org/licenses/gpl-3.0.en.html"),

    LGPL_3_0("GNU Lesser General Public License version 3", "lgpl-3.0", "https://www.gnu.org/licenses/lgpl-3.0.en.html"),

    MIT("MIT License", "mit", "https://opensource.org/licenses/MIT"),

    MPL_2_0("Mozilla Public License Version 2.0", "mpl", "https://www.mozilla.org/en-US/MPL/2.0/"),

    UNLICENSE("Unlicense", "unlicense", "https://unlicense.org/"),

    NONE(null, null, null);

    private String name;

    private String shortName;

    private String url;


    LicenseType(String name, String shortName, String url)
    {
        this.name = name;
        this.shortName = shortName;
        this.url = url;
    }

    public String getName()
    {
        return name;
    }

    public String getShortName()
    {
        return shortName;
    }

    public String getUrl()
    {
        return url;
    }

    public String getLicenseFileSourcePath()
    {
        return "licenses/" + shortName + "/LICENSE.md";
    }

}
