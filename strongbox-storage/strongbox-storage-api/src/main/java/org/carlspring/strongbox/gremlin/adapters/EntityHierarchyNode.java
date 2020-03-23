package org.carlspring.strongbox.gremlin.adapters;

public interface EntityHierarchyNode<T extends EntityHierarchyNode<T>>
{

    T getHierarchyChild();

    T getHierarchyParent();

}
