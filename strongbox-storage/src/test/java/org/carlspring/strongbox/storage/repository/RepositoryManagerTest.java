package org.carlspring.strongbox.storage.repository;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class RepositoryManagerTest
{

    @Autowired
    private RepositoryManager repositoryManager;


    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        final String storageBaseDir = "target/strongbox/storages/storage0";
        File basedir = new File(storageBaseDir);
        File repositoryDir = new File(basedir, "foo-snapshots");

        repositoryManager.createRepositoryStructure(basedir.getAbsolutePath(), "foo-snapshots");
        assertTrue("Failed to create the repository!", repositoryDir.exists());

        repositoryManager.removeRepositoryStructure(basedir.getAbsolutePath(), "foo-snapshots");
        assertFalse("Failed to remove the repository!", repositoryDir.exists());
    }

}
