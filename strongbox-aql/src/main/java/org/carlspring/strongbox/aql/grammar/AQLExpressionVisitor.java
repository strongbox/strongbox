package org.carlspring.strongbox.aql.grammar;

import org.carlspring.strongbox.aql.grammar.AQLParser.TokenKeyContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.TokenValueContext;
import org.carlspring.strongbox.domain.ArtifactEntryExpressionBuilder;

/**
 * @author sbespalov
 *
 */
public class AQLExpressionVisitor extends AQLBaseVisitor<ArtifactEntryExpressionBuilder>
{

    private ArtifactEntryExpressionBuilder expressionBuilder = new ArtifactEntryExpressionBuilder(
            new AqlExpressionDialect());

    @Override
    public ArtifactEntryExpressionBuilder visitTokenValue(TokenValueContext ctx)
    {
        return expressionBuilder.with(ctx.getText());
    }

    @Override
    public ArtifactEntryExpressionBuilder visitTokenKey(TokenKeyContext ctx)
    {
        String attribute = null;
        if (ctx.layoutCoordinateKeyword() != null)
        {
            attribute = ctx.layoutCoordinateKeyword().getText();
        }
        else if (ctx.tokenKeyword() != null)
        {
            attribute = ctx.tokenKeyword().getText();
        }
        return expressionBuilder = expressionBuilder.of(attribute);
    }

    public ArtifactEntryExpressionBuilder getExpressionBuilder()
    {
        return expressionBuilder;
    }

}
