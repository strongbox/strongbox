package org.carlspring.strongbox.gremlin.adapters;

public interface EntityHierarchyNode<T extends EntityHierarchyNode<T>>
{

    default T getHierarchyChild()
    {
        return null;
    }

    default void setHierarchyChild(T node)
    {

    }

    default T getHierarchyParent()
    {
        return null;
    }

    default void setHierarchyParent(T node)
    {

    }

}
