package org.carlspring.strongbox.providers.layout.p2;

import org.xml.sax.Attributes;

import java.util.function.BiConsumer;

interface P2Collector<V>
        extends BiConsumer<String, Attributes>
{

    V get();
}
