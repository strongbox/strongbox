package org.carlspring.strongbox.util;

import org.carlspring.strongbox.artifact.coordinates.RpmArtifactCoordinates;
import org.carlspring.strongbox.domain.RpmPackageType;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class RpmArtifactCoordinatesUtilsTest
{

    @Test
    void parseWhenPackageNameWithPath()
    {
        String packageName = "/usr/lib/test/libqxt-qt5-0.7.0-0.15.20130718giteaf6872f6ad4.fc30.x86_64.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("libqxt-qt5",
                                                   "0.7.0",
                                                   "0.15.20130718giteaf6872f6ad4.fc30",
                                                   "x86_64",
                                                   RpmPackageType.BINARY.getPostfix(),
                                                   "rpm");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());
    }

    @Test
    void parseWhenPackageNameWithoutPath()
    {
        String packageName = "libqxt-qt5-0.7.0-0.15.20130718giteaf6872f6ad4.fc30.x86_64.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("libqxt-qt5",
                                                   "0.7.0",
                                                   "0.15.20130718giteaf6872f6ad4.fc30",
                                                   "x86_64",
                                                   RpmPackageType.BINARY.getPostfix(),
                                                   "rpm");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());
    }

    @Test
    void parseWhenBaseNameContainPlusSymbol()
    {
        String packageName = "gmp-c++-6.1.2-10.fc30.src.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("gmp-c++",
                                                   "6.1.2",
                                                   "10.fc30",
                                                   null,
                                                   "src");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());
    }

    @Test
    void parseWhenBaseNameContainHyphenSymbol()
    {
        String packageName = "urw-base35-d050000l-fonts-20170801-12.fc30.noarch.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("urw-base35-d050000l-fonts",
                                                   "20170801",
                                                   "12.fc30",
                                                   "noarch",
                                                   RpmPackageType.BINARY.getPostfix(),
                                                   "rpm");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());
    }

    @Test
    void parseWhenReleaseContainPlusSymbol()
    {
        String packageName = "cdi-api-1.2-8.module_f28+3939+dc18cd75.noarch.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("cdi-api",
                                                   "1.2",
                                                   "8.module_f28+3939+dc18cd75",
                                                   "noarch",
                                                   RpmPackageType.BINARY.getPostfix(),
                                                   "rpm");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());

    }

    @Test
    void parseWhenReleaseContainDotSymbol()
    {
        String packageName = "libglvnd-1.1.0-4.gitf92208b.fc30.i686.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("libglvnd",
                                                   "1.1.0",
                                                   "4.gitf92208b.fc30",
                                                   "i686",
                                                   RpmPackageType.BINARY.getPostfix(),
                                                   "rpm");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());

    }

    @Test
    void parseSourcePackageWhenArchIsNotProvided()
    {
        String packageName = "libstdc++-9.2.1-1.fc30.src.rpm";

        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinatesUtils.parse(packageName);
        List<String> expectedCoord = Arrays.asList("libstdc++",
                                                   "9.2.1",
                                                   "1.fc30",
                                                   null,
                                                   "src");

        assertThat(expectedCoord.get(0)).isEqualTo(actualCoord.getId());
        assertThat(expectedCoord.get(1)).isEqualTo(actualCoord.getVersion());
        assertThat(expectedCoord.get(2)).isEqualTo(actualCoord.getRelease());
        assertThat(expectedCoord.get(3)).isEqualTo(actualCoord.getArchitecture());
        assertThat(expectedCoord.get(4)).isEqualTo(actualCoord.getPackageType());
    }


    @Test
    void parseNativeWhenArchIsNotCorrect()
    {
        String packageName = "egl-wayland-1.1.3-1.fc30.x81111.rpm";

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RpmArtifactCoordinatesUtils.parse(packageName))
                .withMessageContaining("Incorrect filename: package should have architecture or SRC suffix");
    }

    @Test
    void parseNativeWhenArchIsNotProvided()
    {
        String packageName = "totem-3.32.1-1.fc30.rpm";

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RpmArtifactCoordinatesUtils.parse(packageName))
                .withMessageContaining("Incorrect filename: package should have architecture or SRC suffix");

    }
}
