package org.carlspring.strongbox.providers.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;

/**
 * @author sbespalov
 */
public class ParallelDownloadRemoteArtifactTest
        extends MockedRestArtifactResolverTestBase
        implements ArtifactResolverContext
{

    private static final String REPOSITORY = "pdrat-repository";

    private static final String PROXY_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";

    private static AtomicInteger concurrencyWorkerInstanceHolder = new AtomicInteger();

    private static AtomicInteger concurrentWorkerExecutionCount = new AtomicInteger();

    private RemoteArtifactInputStreamStub remoteArtifactInputStream;

    private Map<InputStream, Thread> remoteRepositoryConnectionOwnerMap = new ConcurrentHashMap<>();

    private int concurrency = Runtime.getRuntime().availableProcessors();


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

    @Override
    @BeforeEach
    public void setup()
            throws IOException
    {
        super.setup();

        remoteArtifactInputStream = new RemoteArtifactInputStreamStub(jarArtifact);
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testConcurrentDownload(@MavenRepository(repositoryId = REPOSITORY)
                                       @Remote(url = PROXY_REPOSITORY_URL)
                                       Repository proxyRepository)
        throws Exception
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC("org.apache.commons:commons-lang3:3.0");
        String path = MavenArtifactUtils.convertArtifactToPath(artifact);
        RepositoryPath artifactPath = repositoryPathResolver.resolve(proxyRepository,
                                                                     path);

        // given
        assertThat(Files.exists(artifactPath)).isFalse();
        assertThat(remoteRepositoryConnectionOwnerMap.containsKey(remoteArtifactInputStream)).isFalse();

        // when
        List<Throwable> result = IntStream.range(0, concurrency)
                                          .mapToObj(i -> new WorkerThread(storageId, repositoryId, path))
                                          .peek(t -> {
                                              t.start();
                                              System.err.println(String.format("Started [%s]", t.getName()));
                                          })
                                          //Parallel execution needed to break sequential `map` method call, to make worker threads runs in parallel 
                                          .parallel()
                                          .peek(t -> {
                                              try
                                              {
                                                  t.join();
                                              }
                                              catch (InterruptedException e)
                                              {
                                                  t.setResult(e);
                                              }
                                          })
                                          .map(WorkerThread::getResult)
                                          .collect(Collectors.toList());

        Throwable[] expected = IntStream.range(0, concurrency)
                                        .mapToObj(i -> (Throwable) null)
                                        .toArray(n -> new Throwable[concurrency]);
        Throwable[] actual = result.toArray(new Throwable[concurrency]);

        // then
        assertThat(Files.exists(artifactPath)).isTrue();
        assertThat(Files.size(artifactPath)).isEqualTo(Files.size(jarArtifact.getFile().toPath()));
        assertThat(result).hasSize(concurrency);

        assertThat(actual).isEqualTo(expected);
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                       repositoryId,
                                                                       path);

        assertThat(repositoryPath.getArtifactEntry()).isNotNull();
        assertThat(repositoryPath.getArtifactEntry().getDownloadCount()).isGreaterThan(0).isLessThanOrEqualTo(Integer.valueOf(concurrency));
        
        assertThat(concurrentWorkerExecutionCount).as("Worker execution was not concurrent.").isNotEqualTo(concurrency);
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
                try
                {
                    artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                                        repositoryId,
                                                                        path);
                }
                catch (AssertionError e)
                {
                    result = e;
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                    result = e;
                }

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
        
        @Override
        public void close()
            throws IOException
        {
            super.close();
            remoteArtifactInputStream = new RemoteArtifactInputStreamStub(jarArtifact);
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

            assertThat(Optional.ofNullable(ownerThread).orElse(currentThread)).isEqualTo(currentThread);
        }
    }

}
