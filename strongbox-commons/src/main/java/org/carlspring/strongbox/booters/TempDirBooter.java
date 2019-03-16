package org.carlspring.strongbox.booters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    private Path lockFile;
    
    @PostConstruct
    public void initialize()
            throws IOException
    {
        lockFile = Paths.get(propertiesBooter.getVaultDirectory()).resolve("temp-dir-booter.lock");

        if (!lockExists())
        {
            createLockFile();
            createTempDir();
        }
    }
    
    public void createTempDir()
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
    
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void createLockFile()
            throws IOException
    {
        Files.createDirectories(lockFile.getParent());
        Files.createFile(lockFile);

        logger.debug(" -> Created lock file '" + lockFile.toAbsolutePath().toString() + "'...");
    }
    
    private boolean lockExists()
    {
        if (Files.exists(lockFile))
        {
            logger.debug(" -> Lock found: '" + propertiesBooter.getVaultDirectory() + "'!");

            return true;
        }
        else
        {
            logger.debug(" -> No lock found.");

            return false;
        }
    }
    
    @PreDestroy
    public void removeLock()
            throws IOException
    {
        Files.deleteIfExists(lockFile);

        logger.debug("Removed lock file '" + lockFile.toAbsolutePath().toString() + "'.");
    }

}
