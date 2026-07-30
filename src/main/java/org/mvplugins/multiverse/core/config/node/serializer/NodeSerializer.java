package org.mvplugins.multiverse.core.config.node.serializer;

public interface NodeSerializer<T> {
    T deserialize(Object object, Class<T> type);

    Object serialize(T object, Class<T> type);
}
