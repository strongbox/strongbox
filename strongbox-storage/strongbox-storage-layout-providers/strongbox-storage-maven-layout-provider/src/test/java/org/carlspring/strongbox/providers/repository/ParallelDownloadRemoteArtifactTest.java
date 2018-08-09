package org.carlspring.strongbox.providers.repository;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.carlspring.strongbox.config.MockedRestArtifactResolverTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sbespalov
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockedRestArtifactResolverTestConfig.class)
public class ParallelDownloadRemoteArtifactTest
        extends RetryDownloadArtifactTestBase
{

    private RemoteArtifactInputStreamStub remoteArtifactInputStream;

    public Map<InputStream, Thread> repoteRepositoryConnectionOwnerMap = new ConcurrentHashMap<>();

    @Before
    public void setup()
        throws Exception
    {
        remoteArtifactInputStream = new RemoteArtifactInputStreamStub(jarArtifact);
        prepareArtifactResolverContext(remoteArtifactInputStream, true);
    }

    @Test
    public void testConcurrentDownload()
        throws Exception
    {
        int concurrency = 8;

        final String storageId = "storage-common-proxies";
        final String repositoryId = "maven-central";
        final String path = "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar";
        final Path destinationPath = getVaultDirectoryPath().resolve("storages")
                                                            .resolve(storageId)
                                                            .resolve(repositoryId)
                                                            .resolve(path);

        // given
        assertFalse(Files.exists(destinationPath));
        assertFalse(repoteRepositoryConnectionOwnerMap.containsKey(remoteArtifactInputStream));

        // when
        List<Throwable> result = IntStream.range(0, concurrency)
                                          .parallel()
                                          .mapToObj(i -> executeTask(storageId, repositoryId, path))
                                          .collect(Collectors.toList());

        Throwable[] actual = IntStream.range(0, concurrency)
                                      .mapToObj(i -> (Throwable) null)
                                      .toArray(n -> new Throwable[concurrency]);
        Throwable[] expected = result.toArray(new Throwable[concurrency]);

        // then
        assertTrue(Files.exists(destinationPath));
        assertThat(Files.size(destinationPath), CoreMatchers.equalTo(Files.size(jarArtifact.getFile().toPath())));
        assertEquals(concurrency, result.size());

        assertArrayEquals(expected, actual);
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);
        assertNotNull(repositoryPath.getArtifactEntry());
        assertEquals(Integer.valueOf(concurrency), repositoryPath.getArtifactEntry().getDownloadCount());

    }

    private Throwable executeTask(final String storageId,
                                  final String repositoryId,
                                  final String path)
    {

        try
        {
            assertStreamNotNull(storageId, repositoryId, path);
        }
        catch (AssertionError e)
        {
            return e;
        }
        catch (Throwable e)
        {
            return e;
        }
        return null;
    }

    private class RemoteArtifactInputStreamStub
            extends FilterInputStream
    {

        private long readCount = 0;

        public RemoteArtifactInputStreamStub(final Resource jarArtifact)
            throws IOException
        {
            super(jarArtifact.getInputStream());
        }

        @Override
        public int read()
            throws IOException
        {
            verifyRead();

            return super.read();
        }

        @Override
        public int read(byte[] b)
            throws IOException
        {
            verifyRead();

            return super.read(b);
        }

        @Override
        public int read(byte[] b,
                        int off,
                        int len)
            throws IOException
        {
            verifyRead();

            return super.read(b, off, len);
        }

        private void verifyRead()
            throws IOException
        {
            if (readCount == 0)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    throw new IOException(e);
                }
            }
            readCount++;

            Thread currentThread = Thread.currentThread();
            Thread ownerThread = repoteRepositoryConnectionOwnerMap.putIfAbsent(this, currentThread);

            assertEquals(currentThread, Optional.ofNullable(ownerThread).orElse(currentThread));
        }

    }

}
