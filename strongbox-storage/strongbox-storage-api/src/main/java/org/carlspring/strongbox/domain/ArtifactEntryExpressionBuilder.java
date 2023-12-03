package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.criteria.ExpressionBuilder;
import org.carlspring.strongbox.data.criteria.ExpressionDialect;

/**
 * @author sbespalov
 *
 */
public class ArtifactEntryExpressionBuilder extends ExpressionBuilder<ArtifactEntryExpressionBuilder, ArtifactEntry>
{

    public ArtifactEntryExpressionBuilder(ExpressionDialect dialect)
    {
        super(ArtifactEntry.class, dialect);
    }

    public ArtifactEntryExpressionBuilder()
    {
        super(ArtifactEntry.class);
    }

    @Override
    public ArtifactEntryExpressionBuilder of(String attribute)
    {
        return super.of(attribute);
    }
    
}
