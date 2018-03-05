package org.carlspring.strongbox.controllers.maven;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.index.context.IndexingContext;

/**
 * We need to support .index/nexus-maven-repository-index.XXX.gz requests
 * but the real path is under `local` sub-path
 *
 * @author Przemyslaw Fusik
 */
public class MavenIndexPathTransformer
        implements Function<String, String>
{

    private static final String INDEX_BASE_PATH = ".index/";

    private static final String INDEX_LOCAL_BASE_PATH = INDEX_BASE_PATH + "local/";

    private static final Pattern INDEX_FILE_PATTERN = Pattern.compile(
            "^" + INDEX_BASE_PATH + "(" + IndexingContext.INDEX_FILE_PREFIX + "(\\.[0-9]+)?" + ".gz|" +
            IndexingContext.INDEX_REMOTE_PROPERTIES_FILE + ")$");

    private static MavenIndexPathTransformer INSTANCE = new MavenIndexPathTransformer();


    private MavenIndexPathTransformer()
    {

    }

    public static MavenIndexPathTransformer getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String apply(final String requestedPath)
    {
        String path = requestedPath;

        if (INDEX_FILE_PATTERN.matcher(path).matches())
        {
            path = INDEX_LOCAL_BASE_PATH + StringUtils.substringAfterLast(path, INDEX_BASE_PATH);
        }
        return path;
    }
}
