package org.carlspring.strongbox.nuget.filter;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.carlspring.strongbox.data.criteria.Predicate;

public class NugetODataFilterParserTemplate extends BaseErrorListener
{

    private Predicate predicate;

    private StringBuilder sb = new StringBuilder();

    public NugetODataFilterParserTemplate(Predicate predicate)
    {
        super();
        this.predicate = predicate;
    }

    public Predicate parseFilterExpression(String filter)
    {
        CodePointCharStream is = CharStreams.fromString(filter);
        NugetODataFilterLexer lexer = new NugetODataFilterLexer(is);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        NugetODataFilterParser parser = new NugetODataFilterParser(commonTokenStream);
        parser.addErrorListener(this);

        NugetODataFilterParser.FilterContext fileContext = parser.filter();

        if (hasErrors())
        {
            throw new NugetODataFilterParserException(getMessage());
        }

        NugetODataFilterVisitor<Predicate> visitor = new NugetODataFilterVisitorImpl(predicate);
        return visitor.visitFilter(fileContext);
    }

    public String getMessage()
    {
        return sb.toString();
    }

    public Boolean hasErrors()
    {
        return sb.length() > 0;
    }
}
