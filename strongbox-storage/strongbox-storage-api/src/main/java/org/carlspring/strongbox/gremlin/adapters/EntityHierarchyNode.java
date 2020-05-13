package org.carlspring.strongbox.gremlin.adapters;

public interface EntityHierarchyNode<T extends EntityHierarchyNode<T>>
{

    T getHierarchyChild();
    
    default void setHierarchyChild(T node) {
        
    }
    
    T getHierarchyParent();
    
    default void setHierarchyParent(T node) {
        
    }

}
