package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.carmatechnologies.commons.testing.logging.ExpectedLogs;
import com.carmatechnologies.commons.testing.logging.api.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GroupRepositoryProviderTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

    @Autowired
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Autowired
    private ConfigurationManager configurationManager;

    @Rule
    public final ExpectedLogs logs = new ExpectedLogs()
    {{
        captureFor(GroupRepositoryProvider.class, LogLevel.DEBUG);
    }};


    @Before
    public void init()
            throws Exception
    {
        super.init();

        createTestRepositoryWithArtifacts(STORAGE0,
                                          "grpt-releases-1",
                                          false,
                                          "com.artifacts.in.releases.one:foo",
                                          "1.2.3");

        createTestRepositoryWithArtifacts(STORAGE0,
                                          "grpt-releases-2",
                                          false,
                                          "com.artifacts.in.releases.two:foo",
                                          "1.2.4");

        Repository repositoryGroup = new Repository("grpt-releases-group");
        repositoryGroup.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setIndexingEnabled(false);
        repositoryGroup.addRepositoryToGroup("grpt-releases-1");
        repositoryGroup.addRepositoryToGroup("grpt-releases-2");

        createRepository(repositoryGroup);

        Repository repositoryWithNestedGroupLevel1 = new Repository("grpt-releases-group-with-nested-group-level-1");
        repositoryWithNestedGroupLevel1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repositoryWithNestedGroupLevel1.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel1.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel1.setAllowsDelete(false);
        repositoryWithNestedGroupLevel1.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel1.setIndexingEnabled(false);
        repositoryWithNestedGroupLevel1.addRepositoryToGroup("grpt-releases-group");

        createRepository(repositoryWithNestedGroupLevel1);

        Repository repositoryWithNestedGroupLevel2 = new Repository("grpt-releases-group-with-nested-group-level-2");
        repositoryWithNestedGroupLevel2.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repositoryWithNestedGroupLevel2.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel2.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel2.setAllowsDelete(false);
        repositoryWithNestedGroupLevel2.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel2.setIndexingEnabled(false);
        repositoryWithNestedGroupLevel2.addRepositoryToGroup("grpt-releases-group-with-nested-group-level-1");

        createRepository(repositoryWithNestedGroupLevel2);

        generateArtifact(getRepositoryBasedir(STORAGE0, "grpt-releases-2").getAbsolutePath(),
                         "org.carlspring.metadata.by.juan:juancho:1.2.64");

        // Used by the testGroupExcludesWildcardRule() test
        generateArtifact(getRepositoryBasedir(STORAGE0, "grpt-releases-1").getAbsolutePath(),
                         "com.artifacts.denied.by.wildcard:foo:1.2.6");
        // Used by the testGroupExcludesWildcardRule() test
        generateArtifact(getRepositoryBasedir(STORAGE0, "grpt-releases-2").getAbsolutePath(),
                         "com.artifacts.denied.by.wildcard:foo:1.2.7");

        createRoutingRules();
    }

    private void createRoutingRules()
    {
        /**
         <accepted>
             <rule-set group-repository="*">
                 <rule pattern=".*(com|org)/artifacts.in.releases1.*">
                     <repositories>
                         <repository>releases</repository>
                     </repositories>
                 </rule>
             </rule-set>
         </accepted>
         */
        createRoutingRuleSet(STORAGE0,
                             "*",
                             new String[]{ "grpt-releases-1" },
                             ".*(com|org)/artifacts.in.releases.one.*",
                             ROUTING_RULE_TYPE_ACCEPTED);

        /**
        <accepted>
            <rule-set group-repository="group-releases">
                <rule pattern=".*(com|org)/artifacts.in.releases.*">
                    <repositories>
                        <repository>releases-with-trash</repository>
                        <repository>releases-with-redeployment</repository>
                    </repositories>
                </rule>
            </rule-set>
         **/
        createRoutingRuleSet(STORAGE0,
                             "grpt-releases-group",
                             new String[]{ "grpt-releases-1", "grpt-releases-2" },
                             ".*(com|org)/artifacts.in.releases.*",
                             ROUTING_RULE_TYPE_ACCEPTED);

        /**
        <denied>
            <rule-set group-repository="*">
                 <rule pattern=".*(com|org)/artifacts.denied.by.wildcard.*">
                     <repositories>
                         <repository>releases</repository>
                     </repositories>
                 </rule>
            </rule-set>
        </denied>
         **/
        createRoutingRuleSet(STORAGE0,
                             "*",
                             new String[]{ "grpt-releases-1" },
                             ".*(com|org)/artifacts.denied.by.wildcard.*",
                             ROUTING_RULE_TYPE_DENIED);
    }

    @Override
    public Map<String, String> getRepositoriesToClean()
    {
        Map<String, String> repositories = new LinkedHashMap<>();
        repositories.put(STORAGE0, "grpt-releases-1");
        repositories.put(STORAGE0, "grpt-releases-2");

        return repositories;
    }

    @After
    public void tearDown()
            throws Exception
    {
        Repository repository = configurationManager.getRepository("storage0:grpt-releases-1");
        if (!repository.isInService())
        {
            repository.putInService();
        }
    }

    @Test
    public void testGroupIncludes()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes...");

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group",
                                                           "com/artifacts/in/releases/one/foo/1.2.3/foo-1.2.3.jar");

        assertNotNull(is);

        is = repositoryProvider.getInputStream(STORAGE0,
                                               "grpt-releases-group",
                                               "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWithOutOfServiceRepository()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with out of service repository...");

        configurationManager.getConfiguration().getStorage(STORAGE0)
                            .getRepository("grpt-releases-2").putOutOfService();

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group",
                                                           "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        configurationManager.getConfiguration().getStorage(STORAGE0)
                            .getRepository("grpt-releases-2").putInService();

        assertNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWildcardRule()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with wildcard...");

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group",
                                                           "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group-with-nested-group-level-1");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group-with-nested-group-level-1",
                                                           "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }
    
    @Test
    public void testGroupAgainstNestedRepository()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group-with-nested-group-level-2");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group-with-nested-group-level-2",
                                                           "org/carlspring/metadata/by/juan/juancho/1.2.64/juancho-1.2.64.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupExcludes()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group excludes...");

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group",
                                                           "com/artifacts/denied/in/memory/foo/1.2.5/foo-1.2.5.jar");

        assertNull(is);
    }

    @Test
    public void testGroupExcludesWildcardRule()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group excludes with wildcard...");

        Repository repository = configurationManager.getRepository("storage0:grpt-releases-group");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream(STORAGE0,
                                                           "grpt-releases-group",
                                                           "com/artifacts/denied/by/wildcard/foo/1.2.6/foo-1.2.6.jar");

        assertNull(is);

        // This one should work, as it's in a different repository
        is = repositoryProvider.getInputStream(STORAGE0,
                                               "grpt-releases-group",
                                               "com/artifacts/denied/by/wildcard/foo/1.2.7/foo-1.2.7.jar");

        assertNotNull(is);
    }

}
