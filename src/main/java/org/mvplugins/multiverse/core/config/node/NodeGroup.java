package org.mvplugins.multiverse.core.config.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.townyadvanced.commentedconfiguration.setting.CommentedNode;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NodeGroup implements Collection<Node> {
    private final Collection<Node> nodes;
    private final List<String> nodeNames;
    private final Map<String, Node> nodesMap;

    public NodeGroup() {
        this.nodes = new ArrayList<>();
        this.nodeNames = new ArrayList<>();
        this.nodesMap = new HashMap<>();
    }

    public NodeGroup(@NotNull Collection<Node> nodes) {
        this.nodes = nodes;
        this.nodesMap = new HashMap<>(nodes.size());
        this.nodeNames = new ArrayList<>(nodes.size());
        nodes.forEach(this::addNodeIndex);
    }

    private void addNodeIndex(@NotNull Node node) {
        if (node instanceof ValueNode<?> valueNode) {
            valueNode.getName().peek(name -> {
                nodeNames.add(name);
                nodesMap.put(name, node);
                Arrays.stream(valueNode.getAliases()).forEach(alias -> nodesMap.put(alias, node));
            });
        }
    }

    private void removeNodeIndex(@NotNull Node node) {
        if (node instanceof ValueNode<?> valueNode) {
            valueNode.getName().peek(name -> {
                nodeNames.remove(name);
                nodesMap.remove(name);
                Arrays.stream(valueNode.getAliases()).forEach(alias -> nodesMap.remove(alias, node));
            });
        }
    }

    public @NotNull Collection<String> getNames() {
        return nodeNames;
    }

    public @NotNull Option<Node> findNode(@Nullable String name) {
        return Option.of(nodesMap.get(name));
    }

    public <T extends Node> @NotNull Option<T> findNode(@Nullable String name, @NotNull Class<T> type) {
        return Option.of(nodesMap.get(name)).map(node -> type.isAssignableFrom(node.getClass()) ? (T) node : null);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return nodes.contains(o);
    }

    @NotNull
    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        return nodes.toArray();
    }

    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] ts) {
        return nodes.toArray(ts);
    }

    @Override
    public boolean add(Node node) {
        if (nodes.add(node)) {
            addNodeIndex(node);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (nodes.remove(o) && o instanceof CommentedNode) {
            removeNodeIndex((Node) o);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return nodes.containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Node> collection) {
        return nodes.addAll(collection);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return nodes.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return nodes.retainAll(collection);
    }

    @Override
    public void clear() {
        nodes.clear();
    }
}
