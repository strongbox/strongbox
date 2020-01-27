package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.criteria.ExpressionBuilder;
import org.carlspring.strongbox.data.criteria.ExpressionDialect;

/**
 * @author sbespalov
 *
 */
public class ArtifactEntryExpressionBuilder extends ExpressionBuilder<ArtifactEntryExpressionBuilder, ArtifactEntity>
{

    public ArtifactEntryExpressionBuilder(ExpressionDialect dialect)
    {
        super(ArtifactEntity.class, dialect);
    }

    public ArtifactEntryExpressionBuilder()
    {
        super(ArtifactEntity.class);
    }

    @Override
    public ArtifactEntryExpressionBuilder of(String attribute)
    {
        return super.of(attribute);
    }
    
}
