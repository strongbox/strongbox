package org.carlspring.strongbox.storage.resolvers;

import com.carmatechnologies.commons.testing.logging.ExpectedLogs;
import com.carmatechnologies.commons.testing.logging.api.LogLevel;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class GroupLocationResolverTest
        extends TestCaseWithArtifactGeneration
{

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(STORAGE_BASEDIR, "releases");

    private static final File REPOSITORY_BASEDIR_RELEASES_WITH_TRASH = new File(STORAGE_BASEDIR, "releases-with-trash");

    private static final File REPOSITORY_BASEDIR_RELEASES_IN_MEMORY = new File(STORAGE_BASEDIR, "releases-in-memory");

    @Autowired
    private GroupLocationResolver groupLocationResolver;

    @Rule
    public final ExpectedLogs logs = new ExpectedLogs()
    {{
        captureFor(GroupLocationResolver.class, LogLevel.DEBUG);
    }};

    public static boolean INITIALIZED = false;


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            generateArtifact(REPOSITORY_BASEDIR_RELEASES_WITH_TRASH.getAbsolutePath(), "com.artifacts.in.releases.with.trash:foo:1.2.3");
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "com.artifacts.in.releases:foo:1.2.4");
            generateArtifact(REPOSITORY_BASEDIR_RELEASES_IN_MEMORY.getAbsolutePath(), "com.artifacts.denied.in.memory:foo:1.2.5");
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "com.artifacts.denied.by.wildcard:foo:1.2.6");

            INITIALIZED = true;
        }
    }

    @Test
    public void testGroupIncludes()
            throws IOException
    {
        System.out.println("# Testing group includes...");

        InputStream is = groupLocationResolver.getInputStream("storage0",
                                                              "group-releases",
                                                              "com/artifacts/in/releases/with/trash/foo/1.2.3/foo-1.2.3.jar");

        assertNotNull(is);

        assertThat(logs.contains("Located artifact via routing rule [storage0:releases-with-trash]:" +
                                 " [+]: .*(com|org)/artifacts.in.releases.with.trash.* after 1 hops."), is(true));

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWildcardRule()
            throws IOException
    {
        System.out.println("# Testing group includes with wildcard...");

        InputStream is = groupLocationResolver.getInputStream("storage0",
                                                              "group-releases",
                                                              "com/artifacts/in/releases/foo/1.2.4/foo-1.2.4.jar");

        assertThat(logs.contains("Located artifact via wildcard routing rule [storage0:releases]:" +
                                 " [+]: .*(com|org)/artifacts.in.releases.* after 1 hops."), is(true));

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository()
            throws IOException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        InputStream is = groupLocationResolver.getInputStream("storage0",
                                                              "group-releases-nested",
                                                              "com/artifacts/in/releases/foo/1.2.4/foo-1.2.4.jar");

        assertThat(logs.contains("Located artifact via wildcard routing rule [storage0:releases]:" +
                                 " [+]: .*(com|org)/artifacts.in.releases.* after 1 hops."), is(true));

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupExcludes()
            throws IOException
    {
        System.out.println("# Testing group excludes...");

        InputStream is = groupLocationResolver.getInputStream("storage0",
                                                              "group-releases",
                                                              "com/artifacts/denied/in/memory/foo/1.2.5/foo-1.2.5.jar");

        assertThat(logs.contains("releases-in-memory/com/artifacts/denied/in/memory/foo/1.2.5/foo-1.2.5.jar"), is(false));

        assertNull(is);
    }

    @Test
    public void testGroupExcludesWildcardRule()
            throws IOException
    {
        System.out.println("# Testing group excludes with wildcard...");

        InputStream is = groupLocationResolver.getInputStream("storage0",
                                                              "group-releases",
                                                              "com/artifacts/denied/by/wildcard/foo/1.2.6/foo-1.2.6.jar");

        assertThat(logs.contains("releases/com/artifacts/denied/by/wildcard/foo/1.2.6/foo-1.2.6.jar"), is(false));

        assertNull(is);
    }

}
