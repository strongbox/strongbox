package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sbespalov
 *
 */
public class ParallelDownloadRemoteArtifactTest
        extends MockedRestArtifactResolverTestBase implements ArtifactResolverContext
{
    
    private static final String REPOSITORY = "pdrat-repository";

    private static final String PROXY_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";
    
    private static AtomicInteger concurrencyWorkerInstanceHolder = new AtomicInteger();
    private static AtomicInteger concurrentWorkerExecutionCount = new AtomicInteger();
    private RemoteArtifactInputStreamStub remoteArtifactInputStream;
    private Map<InputStream, Thread> remoteRepositoryConnectionOwnerMap = new ConcurrentHashMap<>();
    private int concurrency = Runtime.getRuntime().availableProcessors();
    
    @Inject
    private PlatformTransactionManager transactionManager;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    public ParallelDownloadRemoteArtifactTest() throws IOException
    {
        remoteArtifactInputStream = new RemoteArtifactInputStreamStub(jarArtifact);
    }

    @Override
    public InputStream getInputStream()
    {
        return remoteArtifactInputStream;
    }

    @Override
    protected ArtifactResolverContext lookupArtifactResolverContext()
    {
        return this;
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testConcurrentDownload(@MavenRepository(repositoryId = REPOSITORY) @Remote(url = PROXY_REPOSITORY_URL) Repository proxyRepository)
        throws Exception
    {
        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC("org.apache.commons:commons-lang3:3.0");
        String path = MavenArtifactUtils.convertArtifactToPath(artifact);
        RepositoryPath artifactPath = repositoryPathResolver.resolve(proxyRepository)
                                                            .resolve(path);

        // given
        assertFalse(Files.exists(artifactPath));
        assertFalse(remoteRepositoryConnectionOwnerMap.containsKey(remoteArtifactInputStream));

        // when
        List<Throwable> result = IntStream.range(0, concurrency)
                                          .mapToObj(i -> new WorkerThread(STORAGE0, REPOSITORY, path))
                                          .map(t -> {
                                              t.start();
                                              System.err.println(String.format("Started [%s]", t.getName()));
                                              return t;
                                          })
                                          //Parallel execution needed to break sequential `map` method call, to make worker threads runs in parallel 
                                          .parallel()
                                          .map(t -> {
                                              try
                                              {
                                                  t.join();
                                              }
                                              catch (InterruptedException e)
                                              {
                                                  t.setResult(e);
                                              }
                                              return t;
                                          })
                                          .map(t -> t.getResult())
                                          .collect(Collectors.toList());

        Throwable[] expected = IntStream.range(0, concurrency)
                                        .mapToObj(i -> (Throwable) null)
                                        .toArray(n -> new Throwable[concurrency]);
        Throwable[] actual = result.toArray(new Throwable[concurrency]);

        // then
        assertTrue(Files.exists(artifactPath));
        assertThat(Files.size(artifactPath), CoreMatchers.equalTo(Files.size(jarArtifact.getFile().toPath())));
        assertEquals(concurrency, result.size());

        assertArrayEquals(expected, actual);
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(STORAGE0, REPOSITORY, path);

        assertNotNull(repositoryPath.getArtifactEntry());
        assertEquals(Integer.valueOf(concurrency), repositoryPath.getArtifactEntry().getDownloadCount());
        
        assertNotEquals(concurrency, concurrentWorkerExecutionCount, "Worker execution was not concurrent.");
    }

    private class WorkerThread extends Thread
    {
        private Throwable result;

        final String storageId;
        final String repositoryId;
        final String path;

        public WorkerThread(String storageId,
                            String repositoryId,
                            String path)
        {
            this.storageId = storageId;
            this.repositoryId = repositoryId;
            this.path = path;
        }

        public Throwable getResult()
        {
            return result;
        }

        public void setResult(Throwable result)
        {
            this.result = result;
        }
        
        @Override
        public void run()
        {
            initContext(ParallelDownloadRemoteArtifactTest.this);
            concurrencyWorkerInstanceHolder.set(hashCode());
            
            try
            {
                result = new TransactionTemplate(transactionManager).execute(t -> {
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
                        e.printStackTrace();
                        return e;
                    }

                    return null;
                });
                if (hashCode() != concurrencyWorkerInstanceHolder.get())
                {
                    concurrentWorkerExecutionCount.incrementAndGet();
                }
            } 
            finally
            {
                cleanContext();
            }
        }

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
            Thread ownerThread = remoteRepositoryConnectionOwnerMap.putIfAbsent(this, currentThread);

            assertEquals(currentThread, Optional.ofNullable(ownerThread).orElse(currentThread));
        }
    }

}
