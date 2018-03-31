package org.carlspring.strongbox.aql.grammar;

import org.carlspring.strongbox.aql.grammar.AQLParser.QueryContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.QueryExpContext;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;

/**
 * @author sbespalov
 *
 */
public class AQLStatementVisitor extends AQLBaseVisitor<Selector<ArtifactEntry>>
{

    private Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

    public AQLStatementVisitor()
    {
    }

    @Override
    public Selector<ArtifactEntry> visitQuery(QueryContext ctx)
    {
        AQLQueryVisitor queryVisitor = new AQLQueryVisitor(Predicate.empty());
        for (QueryExpContext queryExpContext : ctx.queryExp())
        {
            selector.where(queryVisitor.visitQueryExp(queryExpContext));
        }

        AQLPaginatorVisitor aqlPaginatorVisitor = new AQLPaginatorVisitor();
        Paginator paginator = aqlPaginatorVisitor.visitOrderExp(ctx.orderExp());
        paginator = aqlPaginatorVisitor.visitPageExp(ctx.pageExp());
        
        selector.with(paginator);

        return selector;
    }

}
