package org.carlspring.strongbox.nuget.filter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryParser;

/**
 * @author sbespalov
 *
 */
public class NugetODataFilterQueryParser extends QueryParser
{

    public NugetODataFilterQueryParser(String query)
    {
        super(createParser(CharStreams.fromString(query)));
    }

    public static Parser createParser(CharStream is)
    {
        NugetODataFilterLexer lexer = new NugetODataFilterLexer(is);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        return new NugetODataFilterParser(commonTokenStream);
    }

    @Override
    protected ParseTreeVisitor<Predicate> createTreeVisitor()
    {
        return new NugetODataFilterVisitorImpl(Predicate.empty());
    }

    @Override
    protected ParseTree parseQueryTree(Parser parser)
    {
        return ((NugetODataFilterParser) parser).filter();
    }
}
