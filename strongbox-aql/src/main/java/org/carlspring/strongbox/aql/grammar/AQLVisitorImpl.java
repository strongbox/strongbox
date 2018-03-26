package org.carlspring.strongbox.aql.grammar;

import org.antlr.v4.runtime.tree.ParseTree;
import org.carlspring.strongbox.aql.grammar.AQLParser.LogicalOpContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.QueryContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.QueryExpContext;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Predicate.BooleanOperator;

/**
 * @author sbespalov
 *
 */
public class AQLVisitorImpl extends AQLBaseVisitor<Predicate>
{

    private Predicate root;

    public AQLVisitorImpl(Predicate predicate)
    {
        this.root = predicate;
    }

    @Override
    public Predicate visitQuery(QueryContext tree)
    {
        Predicate p = super.visitQuery(tree);
        return root.and(p);
    }

    @Override
    public Predicate visitQueryExp(QueryExpContext ctx)
    {
        if (ctx.tokenExp() != null)
        {
            return visitTokenExp(ctx.tokenExp());
        }
        else if (ctx.vNesteedQueryExp != null)
        {
            return visitQueryExp(ctx.vNesteedQueryExp);
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
            filterExpRoot.and(p1.or(p2));
        }

        return filterExpRoot;
    }

    private BooleanOperator extractBooleanOperator(QueryExpContext ctx)
    {
        LogicalOpContext logicalOpCtx = ctx.logicalOp();
        String sLogicalOperator = logicalOpCtx == null ? BooleanOperator.AND.toString() : logicalOpCtx.getText();
        BooleanOperator booleanOperator = sLogicalOperator == null ? BooleanOperator.AND
                : BooleanOperator.valueOf(sLogicalOperator.toUpperCase());
        return booleanOperator;
    }

}
