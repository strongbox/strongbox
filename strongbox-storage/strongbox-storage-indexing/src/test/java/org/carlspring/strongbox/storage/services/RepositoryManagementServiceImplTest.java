package org.carlspring.strongbox.storage.services;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class RepositoryManagementServiceImplTest
{

    public static final String REPOSITORY_ID = "empty-test-release-repository";

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/" + REPOSITORY_ID);


    @Autowired
    private RepositoryManagementService repositoryManagementService;


    @Test
    public void testCreateRepository()
            throws IOException
    {
        repositoryManagementService.createRepository("storage0", REPOSITORY_ID);

        Assert.assertTrue("Failed to create repository '" + REPOSITORY_ID  + "'!", REPOSITORY_BASEDIR.exists());
    }

}
