package org.carlspring.strongbox.aql.grammar;

import java.util.Optional;

import org.carlspring.strongbox.aql.grammar.AQLParser.QueryExpContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.TokenExpContext;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Predicate.BooleanOperator;
import org.carlspring.strongbox.domain.ArtifactEntryExpressionBuilder;

/**
 * @author sbespalov
 *
 */
public class AqlQueryVisitor extends AQLBaseVisitor<Predicate>
{

    private Predicate root;

    public AqlQueryVisitor(Predicate predicate)
    {
        this.root = predicate;
    }

    public Predicate getRoot()
    {
        return root;
    }

    @Override
    public Predicate visitQueryExp(QueryExpContext ctx)
    {
        if (ctx.tokenExp() != null)
        {
            Predicate p = visitTokenExp(ctx.tokenExp());

            return negatePredicateIfNeeded(ctx, p);
        }
        else if (ctx.vNestedQueryExp != null)
        {
            Predicate p = visitQueryExp(ctx.vNestedQueryExp).nested();

            return negatePredicateIfNeeded(ctx, p);
        }

        BooleanOperator booleanOperator = extractBooleanOperator(ctx);

        Predicate filterExpRoot = Predicate.empty();

        Predicate p1 = visitQueryExp(ctx.vQueryExpLeft);
        Predicate p2 = visitQueryExp(ctx.vQueryExpRight);

        if (BooleanOperator.AND.equals(booleanOperator))
        {
            filterExpRoot.and(p1).and(p2);
        }
        else if (BooleanOperator.OR.equals(booleanOperator))
        {
            filterExpRoot.or(p1).or(p2);
        }

        return filterExpRoot;
    }

    private Predicate negatePredicateIfNeeded(QueryExpContext ctx,
                                              Predicate p)
    {
        return Optional.ofNullable(ctx.tokenPrefix())
                       .map(c -> c.getText())
                       .filter(c -> c.endsWith("!"))
                       .map(c -> p.negated())
                       .orElse(p);
    }

    @Override
    public Predicate visitTokenExp(TokenExpContext ctx)
    {
        AqlExpressionVisitor nestedVisitor = new AqlExpressionVisitor();
        
        nestedVisitor.visitTokenExp(ctx);
        
        //nestedVisitor.visitTokenKey(ctx.tokenKey());
        //nestedVisitor.visitTokenValue(ctx.tokenValue());

        ArtifactEntryExpressionBuilder expressionBuilder = nestedVisitor.getExpressionBuilder();
        expressionBuilder.using(null);

        return Predicate.of(expressionBuilder.build());
    }

    private BooleanOperator extractBooleanOperator(QueryExpContext ctx)
    {
        return Optional.ofNullable(ctx.logicalOp())
                       .map(v -> v.getText())
                       .map(v -> BooleanOperator.of(v.toUpperCase()))
                       .orElse(BooleanOperator.AND);
    }

}
