package org.carlspring.strongbox.gremlin.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl;
import org.strongbox.util.Commons;

public class EntityTraversalUtils
{
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSXXX";

    public static <E> E extractObject(Class<E> target,
                                      Object value)
    {
        return Optional.of(value)
                       .filter(v -> !EntityTraversalDsl.NULL.equals(v))
                       .map(target::cast)
                       .orElse(null);
    }

    public static Date extractDate(Object value)
    {
        return Optional.of(value)
                       .filter(v -> !EntityTraversalDsl.NULL.equals(v))
                       .map(String.class::cast)
                       .map(EntityTraversalUtils::format)
                       .orElse(null);
    }

    protected static Date format(String t)
    {
        try
        {
            return new SimpleDateFormat(DATE_FORMAT).parse(t);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException();
        }
    }

    public static <E> List<E> extracPropertytList(Class<E> target,
                                                  Object value)
    {
        return Optional.of(value)
                       .filter(v -> !EntityTraversalDsl.NULL.equals(v))
                       .map(v -> (List<Property<Object>>) v)
                       .<List<E>>map(c -> c.stream().map(p -> p.value()).map(target::cast).collect(Collectors.toList()))
                       .orElse(null);
    }

    public static <E> Object castToObject(Traverser<E> t)
    {
        return Object.class.cast(t.get());
    }

    public static void traceVertex(Traverser<? extends Element> t)
    {
        System.out.println(String.format("[%s]-[%s]-[%s]", t.get().label(), t.get().id(),
                                         t.get().property("uuid").orElse("empty")));
    }

    public static <T extends DomainObject> List<T> reduceHierarchy(List<T> entityList)
    {
        Map<String, List<T>> resultMapByUuid = entityList.stream()
                                                         .collect(Collectors.groupingBy(DomainObject::getUuid,
                                                                                        Collectors.toCollection(LinkedList::new)));

        return resultMapByUuid.values()
                              .stream()
                              .map(e -> e.stream()
                                         .sorted((a1,
                                                  a2) -> a1.getClass().isInstance(a2) ? 1 : -1)
                                         .findFirst()
                                         .get())
                              .collect(Collectors.toList());

    }

    public static LocalDateTime toLocalDateTime(Date date)
    {
        return Commons.toLocalDateTime(date);
    }

    public static Date toDate(LocalDateTime date)
    {
        return Commons.toDate(date);
    }

    public static LocalDateTime toLocalDateTime(Long value)
    {
        if (value == null)
        {
            return null;
        }
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Long toLong(LocalDateTime date)
    {
        if (date == null)
        {
            return null;
        }
        return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
