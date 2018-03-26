package org.carlspring.strongbox.aql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.carlspring.strongbox.aql.grammar.AqlQueryParser;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AqlParserTestCase
{

    private static final Logger logger = LoggerFactory.getLogger(AqlParserTestCase.class);

    @Test
    public void testValidQuery()
        throws Exception
    {
        String query = "(storage:storage0) +repository:releases OR +(groupId:'org.carlspring') AND (-(artifactId:'some strange group') || -version:'0.*')";
        AqlQueryParser aqlParser = new AqlQueryParser(query);
        Predicate predicate = aqlParser.parseQuery();

        logger.info(String.format("Query [%s] parse tree:\n[%s]", query, aqlParser));

        Assert.assertFalse(aqlParser.hasErrors());
        
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
