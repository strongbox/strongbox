package org.carlspring.strongbox.aql.grammar;

import org.carlspring.strongbox.aql.grammar.AQLParser.QueryContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.QueryExpContext;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntity;

/**
 * @author sbespalov
 *
 */
public class AqlStatementVisitor extends AQLBaseVisitor<Selector<ArtifactEntity>>
{

    private Selector<ArtifactEntity> selector = new Selector<>(ArtifactEntity.class);

    public AqlStatementVisitor()
    {
    }

    @Override
    public Selector<ArtifactEntity> visitQuery(QueryContext ctx)
    {
        Predicate artifactPredicate = Predicate.of(ExpOperator.IS_NOT_NULL.of("artifactCoordinates"));
        AqlQueryVisitor queryVisitor = new AqlQueryVisitor(artifactPredicate);

        for (QueryExpContext queryExpContext : ctx.queryExp())
        {
            artifactPredicate.and(queryVisitor.visitQueryExp(queryExpContext).nested());
        }
        selector.where(queryVisitor.getRoot());

        AqlPaginatorVisitor aqlPaginatorVisitor = new AqlPaginatorVisitor();
        Paginator paginator = aqlPaginatorVisitor.visitOrderExp(ctx.orderExp());
        paginator = aqlPaginatorVisitor.visitPageExp(ctx.pageExp());

        selector.with(paginator);

        return selector;
    }

}
