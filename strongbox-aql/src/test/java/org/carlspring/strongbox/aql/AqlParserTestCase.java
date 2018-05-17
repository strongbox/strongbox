package org.carlspring.strongbox.aql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.carlspring.strongbox.aql.grammar.AqlQueryParser;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class AqlParserTestCase
{

    private static final Logger logger = LoggerFactory.getLogger(AqlParserTestCase.class);

    @Test
    public void testValidQuery()
        throws Exception
    {
        String query = "(storage:storage-common-proxies) +repository:carlspring OR +(groupId:'org.carlspring') AND (-(artifactId:'some strange group') || -version:'0.*') asc: age skip: 12";
        AqlQueryParser aqlParser = new AqlQueryParser(query);

        logger.info(String.format("Query [%s] parse tree:\n[%s]", query, aqlParser));

        Selector<ArtifactEntry> selector = aqlParser.parseQuery();
        Predicate predicate = selector.getPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertFalse(predicate.isEmpty());
        Assert.assertFalse(aqlParser.hasErrors());

        OQueryTemplate<Object, ArtifactEntry> queryTemplate = new OQueryTemplate<>(null);

        String sqlQuery = queryTemplate.calculateQueryString(selector);
        logger.info(String.format("Query [%s] parse result:\n[%s]", query, sqlQuery));
        Assert.assertEquals("SELECT * " +
                "FROM " +
                "ArtifactEntry " +
                "WHERE " +
                " NOT artifactCoordinates IS NULL  " +
                "AND ((storageId = :storageId_0) " +
                "AND repositoryId = :repositoryId_1 " +
                "OR (artifactCoordinates.coordinates.groupId = :groupId_1) " +
                "AND ( NOT (artifactCoordinates.coordinates.artifactId = :artifactId_1) OR  NOT artifactCoordinates.version LIKE :version_2)) " +
                "ORDER BY lastUpdated ASC " +
                "SKIP 12 " +
                "LIMIT 25", sqlQuery);

        Map<String, Object> parameterMap = queryTemplate.exposeParameterMap(predicate);
        logger.info(String.format("Query [%s] parse parameters:\n[%s]", query, parameterMap));
        Assert.assertEquals(ImmutableMap.of("storageId_0", "storage-common-proxies", "repositoryId_1", "carlspring", "groupId_1",
                                            "org.carlspring", "version_2", "0.%", "artifactId_1", "some strange group"),
                            parameterMap);
    }

    @Test
    public void testInvalidQuery()
        throws Exception
    {
        String query = "[storage:storage0] ++repository0:releases ||| & groupId:'org.carlspring')";

        AqlQueryParser aqlParser = new AqlQueryParser(query);
        Map<Pair<Integer, Integer>, String> errorMap = null;
        try
        {
            aqlParser.parseQuery();
        }
        catch (QueryParserException e)
        {
            errorMap = aqlParser.getErrors();
        }
        logger.info(String.format("Query [%s] parse tree:\n[%s]", query, aqlParser));

        Assert.assertTrue(aqlParser.hasErrors());
        Assert.assertNotNull(errorMap);
        Assert.assertEquals(5, errorMap.size());

        List<Pair<Integer, Integer>> errorPositionList = new ArrayList<>(errorMap.keySet());
        Assert.assertEquals(Pair.with(1, 0), errorPositionList.get(0));
        Assert.assertEquals(Pair.with(1, 17), errorPositionList.get(1));
        Assert.assertEquals(Pair.with(1, 20), errorPositionList.get(2));
        Assert.assertEquals(Pair.with(1, 44), errorPositionList.get(3));
        Assert.assertEquals(Pair.with(1, 46), errorPositionList.get(4));
    }

}
