package org.janusgraph.graphdb.database.idassigner.placement;

import java.util.Map;

import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.diskstorage.configuration.Configuration;
import org.janusgraph.graphdb.configuration.PreInitializeConfigOptions;
import org.janusgraph.graphdb.internal.InternalElement;
import org.janusgraph.graphdb.internal.InternalVertex;

@PreInitializeConfigOptions
public class StaticPlacementStrategy extends SimpleBulkPlacementStrategy
{

    private static final int PID = 1;

    public StaticPlacementStrategy(Configuration config)
    {
        super(config);
    }

    public StaticPlacementStrategy(String key, int concurrentPartitions)
    {
        super(concurrentPartitions);
    }

    @Override
    public int getPartition(InternalElement element)
    {
        if (element instanceof JanusGraphVertex)
        {
            return PID;
        }
        
        return super.getPartition(element);
    }

    @Override
    public void getPartitions(Map<InternalVertex, PartitionAssignment> vertices)
    {
        super.getPartitions(vertices);
        
        for (Map.Entry<InternalVertex, PartitionAssignment> entry : vertices.entrySet())
        {
            ((SimplePartitionAssignment)entry.getValue()).setPartitionID(PID);
        }
    }

}
