package org.carlspring.strongbox.testing;

import java.io.File;

import org.junit.Test;
import static junit.framework.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class StrongboxDirectoryLayoutManagerTest
{

    @Test
    public void testCreateDirectoryStructures()
    {
        String strongboxBasedir = "target/strongbox";

        StrongboxDirectoryLayoutManager.createDirectoryStructures(strongboxBasedir);

        final File basedir = new File(strongboxBasedir);
        final File releases = new File(strongboxBasedir + "/storages/storage0", "releases");
        final File snapshots = new File(strongboxBasedir + "/storages/storage0", "snapshots");

        assertTrue("Failed to create basedir!", basedir.exists() && basedir.isDirectory());
        assertTrue("Failed to create releases repository!", releases.exists() && releases.isDirectory());
        assertTrue("Failed to create snapshots repository!", snapshots.exists() && snapshots.isDirectory());
    }

}
