package org.carlspring.strongbox.nuget.filter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.carlspring.strongbox.data.criteria.QueryParser;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.nuget.filter.NugetODataFilterParser.FilterContext;

/**
 * @author sbespalov
 *
 */
public class NugetODataFilterQueryParser extends QueryParser<FilterContext, ArtifactEntity, NugetODataFilterVisitorImpl>
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
    protected NugetODataFilterVisitorImpl createTreeVisitor()
    {
        return new NugetODataFilterVisitorImpl();
    }

    @Override
    protected FilterContext parseQueryTree(Parser parser)
    {
        return ((NugetODataFilterParser) parser).filter();
    }
}
