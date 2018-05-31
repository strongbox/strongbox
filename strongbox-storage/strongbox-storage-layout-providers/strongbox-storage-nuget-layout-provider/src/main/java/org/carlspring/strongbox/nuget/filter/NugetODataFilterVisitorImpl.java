package org.carlspring.strongbox.nuget.filter;

import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.nuget.filter.NugetODataFilterParser.FilterContext;

/**
 * @author sbespalov
 *
 */
public class NugetODataFilterVisitorImpl extends NugetODataFilterBaseVisitor<Selector<ArtifactEntry>>
{

    private Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

    public NugetODataFilterVisitorImpl()
    {
        super();
    }

    @Override
    public Selector<ArtifactEntry> visitFilter(FilterContext ctx)
    {
        NugetODataQueryVisitor nugetODataQueryVisitor = new NugetODataQueryVisitor();
        selector.where(nugetODataQueryVisitor.visitFilter(ctx));
        return selector;
    }

}
