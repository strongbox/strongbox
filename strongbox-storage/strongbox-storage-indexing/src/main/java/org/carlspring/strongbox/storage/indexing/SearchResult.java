package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "artifact")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResult
{

    @XmlElement
    private String groupId;

    @XmlElement
    private String artifactId;

    @XmlElement
    private String version;

    @XmlElement
    private String classifier;

    @XmlTransient
    private String extension;

    @XmlElement
    private String repository;

    @XmlElement
    private String path;

    @XmlElement
    private String url;


    public SearchResult()
    {
    }

    public SearchResult(String repository,
                        String groupId,
                        String artifactId,
                        String version,
                        String classifier,
                        String extension,
                        String path,
                        String url)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
        this.repository = repository;
        this.path = path;
        this.url = url;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier(String classifier)
    {
        this.classifier = classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return groupId + ':' + artifactId + ':' + version + ':' + extension + ':' + classifier ;
    }

    public static void main(String[] args)
            throws JAXBException
    {
        SearchResult sr1 = new SearchResult("releases",
                                            "org.foo",
                                            "bar",
                                            "1.0",
                                            null,
                                            "jar",
                                            "org/foo/bar/1.0/bar-1.0.jar",
                                            "http://localhost:48080/storages/storage0/releases/org/foo/bar/1.0/bar-1.0.jar");

        SearchResult sr2 = new SearchResult("releases",
                                            "org.foo",
                                            "bar",
                                            "1.1",
                                            null,
                                            "jar",
                                            "org/foo/bar/1.1/bar-1.1.jar",
                                            "http://localhost:48080/storages/storage0/releases/org/foo/bar/1.1/bar-1.1.jar");

        SearchResult sr3 = new SearchResult("snapshots",
                                            "org.foo",
                                            "bar",
                                            "1.0-SNAPSHOT",
                                            null,
                                            "jar",
                                            "org/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar",
                                            "http://localhost:48080/storages/storage0/snapshots/org/foo/bar/1.0-SNAPSHOT/bar-1.0-SNAPSHOT.jar");

        SearchResult sr4 = new SearchResult("snapshots",
                                            "org.foo",
                                            "bar",
                                            "1.1-SNAPSHOT",
                                            null,
                                            "jar",
                                            "org/foo/bar/1.1-SNAPSHOT/bar-1.1-SNAPSHOT.jar",
                                            "http://localhost:48080/storages/storage0/snapshots/org/foo/bar/1.1-SNAPSHOT/bar-1.1-SNAPSHOT.jar");

        SearchResult sr5 = new SearchResult("snapshots",
                                            "org.foo",
                                            "bar",
                                            "1.2-SNAPSHOT",
                                            null,
                                            "jar",
                                            "org/foo/bar/1.2-SNAPSHOT/bar-1.2-SNAPSHOT.jar",
                                            "http://localhost:48080/storages/storage0/snapshots/org/foo/bar/1.2-SNAPSHOT/bar-1.2-SNAPSHOT.jar");

        Collection<SearchResult> resultsReleases = new LinkedHashSet<>();
        resultsReleases.add(sr1);
        resultsReleases.add(sr2);

        Collection<SearchResult> resultsSnapshots = new LinkedHashSet<>();
        resultsSnapshots.add(sr3);
        resultsSnapshots.add(sr4);
        resultsSnapshots.add(sr5);

        Set<SearchResult> results = new LinkedHashSet<>();
        results.addAll(resultsReleases);
        results.addAll(resultsSnapshots);

        SearchResults searchResults = new SearchResults();
        searchResults.setResults(results);

        // GenericParser<ArrayList<SearchResult>> parser = new GenericParser<>(ArrayList.class);
        GenericParser<SearchResults> parser = new GenericParser<>(SearchResults.class);
        parser.store(searchResults, System.out);
    }

}
