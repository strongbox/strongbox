package org.carlspring.strongbox.nuget.filter;

import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.nuget.filter.NugetODataFilterParser.FilterContext;

/**
 * @author sbespalov
 *
 */
public class NugetODataFilterVisitorImpl extends NugetODataFilterBaseVisitor<Selector<ArtifactEntity>>
{

    private Selector<ArtifactEntity> selector = new Selector<>(ArtifactEntity.class);

    public NugetODataFilterVisitorImpl()
    {
        super();
    }

    @Override
    public Selector<ArtifactEntity> visitFilter(FilterContext ctx)
    {
        NugetODataQueryVisitor nugetODataQueryVisitor = new NugetODataQueryVisitor();
        selector.where(nugetODataQueryVisitor.visitFilter(ctx));
        return selector;
    }

}
