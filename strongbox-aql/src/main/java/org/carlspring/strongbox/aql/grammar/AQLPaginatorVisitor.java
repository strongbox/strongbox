package org.carlspring.strongbox.aql.grammar;

import java.util.Optional;

import org.carlspring.strongbox.aql.grammar.AQLParser.OrderExpContext;
import org.carlspring.strongbox.aql.grammar.AQLParser.PageExpContext;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Paginator.Order;

/**
 * @author sbespalov
 *
 */
public class AQLPaginatorVisitor extends AQLBaseVisitor<Paginator>
{

    private Paginator paginator = new Paginator();

    @Override
    public Paginator visitPageExp(PageExpContext ctx)
    {
        paginator.setSkip(Integer.valueOf(ctx.NUMBER().getText()));

        return paginator;
    }

    @Override
    public Paginator visitOrderExp(OrderExpContext ctx)
    {
        if (Order.DESC.toString().equalsIgnoreCase(ctx.orderDirection().getText()))
        {
            paginator.setOrder(Order.DESC);
        }

        String aqlOrderProperty = ctx.orderValue().getText();

        for (AqlMapping aqlPropertyKeyword : AqlMapping.values())
        {
            if (!aqlPropertyKeyword.toString().equalsIgnoreCase(aqlOrderProperty))
            {
                continue;
            }

            paginator.setProperty(aqlPropertyKeyword.property());

            break;
        }

        paginator.setProperty(Optional.ofNullable(paginator.getProperty())
                                      .orElse(String.format("artifactCoordinates.coordinates.%s",
                                                            aqlOrderProperty)));

        return paginator;
    }

}
