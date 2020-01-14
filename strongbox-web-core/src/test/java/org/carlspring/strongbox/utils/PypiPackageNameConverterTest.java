package org.carlspring.strongbox.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author ankit.tomar
 */
public class PypiPackageNameConverterTest
{

    @Test
    public void testEscapeSpecialCharacters()
    {
        String packageName1 = PypiPackageNameConverter.escapeSpecialCharacters("hello-world-pypi");
        assertThat(packageName1).isNotBlank().isEqualTo("hello_world_pypi");

        String packageName2 = PypiPackageNameConverter.escapeSpecialCharacters("hello_world_pypi");
        assertThat(packageName2).isNotBlank().isEqualTo("hello_world_pypi");
        
        String packageName3 = PypiPackageNameConverter.escapeSpecialCharacters("hello world pypi");
        assertThat(packageName3).isNotBlank().isEqualTo("hello world pypi");
        
        String packageName4 = PypiPackageNameConverter.escapeSpecialCharacters("hello$world$pypi");
        assertThat(packageName4).isNotBlank().isEqualTo("hello_world_pypi");
        
        String packageName5 = PypiPackageNameConverter.escapeSpecialCharacters("hello-1%world_2#pypi");
        assertThat(packageName5).isNotBlank().isEqualTo("hello_1_world_2_pypi");
    }

}
