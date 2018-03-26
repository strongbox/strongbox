package org.carlspring.strongbox.aql.grammar;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.carlspring.strongbox.aql.grammar.AQLParser.QueryContext;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryParser;

/**
 * @author sbespalov
 *
 */
public class AqlQueryParser extends QueryParser<QueryContext, AQLVisitorImpl>
{

    public AqlQueryParser(String query)
    {
        super(createParser(CharStreams.fromString(query)));
    }

    public static Parser createParser(CharStream is)
    {
        AQLLexer lexer = new AQLLexer(is);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        return new AQLParser(commonTokenStream);
    }

    @Override
    protected AQLVisitorImpl createTreeVisitor()
    {
        return new AQLVisitorImpl(Predicate.empty());
    }

    @Override
    protected QueryContext parseQueryTree(Parser parser)
    {
        return ((AQLParser) parser).query();
    }

}
