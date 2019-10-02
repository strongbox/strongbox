package org.carlspring.strongbox.booters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;


/**
 * 
 * @author kalski
 *
 */
public class TempDirBooter
{
    private static final Logger logger = LoggerFactory.getLogger(TempDirBooter.class);
    
    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    private HazelcastInstance hazelcastInstance;


    @PostConstruct
    public void initialize()
            throws IOException
    {
        ILock lock = hazelcastInstance.getLock("TempDirBooterLock");
        if (lock.tryLock())
        {
            logger.debug(" -> No lock found.");

            try
            {
                createTempDir();
            }
            finally
            {
                lock.unlock();

                logger.debug("Removed lock '" + lock.getName());
            }
        }
        else
        {
            logger.debug(" -> Lock found: '" + lock.getName() + "'!");
        }
    }
    
    private void createTempDir()
            throws IOException
    {
        String tempDirLocation = System.getProperty("java.io.tmpdir",
                                                    Paths.get(propertiesBooter.getVaultDirectory(), "tmp")
                                                         .toAbsolutePath()
                                                         .toString());
        Path tempDirPath = Paths.get(tempDirLocation).toAbsolutePath();
        if (!Files.exists(tempDirPath))
        {
            Files.createDirectories(tempDirPath);
        }

        logger.debug("Temporary directory: " + tempDirPath.toString() + ".");

        if (System.getProperty("java.io.tmpdir") == null)
        {
            System.setProperty("java.io.tmpdir", tempDirPath.toString());

            logger.debug("Set java.io.tmpdir to " + tempDirPath.toString() + ".");
        }
        else
        {
            logger.debug("The java.io.tmpdir is already set to " + System.getProperty("java.io.tmpdir") + ".");
        }
    }
}
