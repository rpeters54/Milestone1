package instructions;

import ast.BasicBlock;

import java.util.*;

public class Graph<T> {

    private final Map<T, List<T>> nodes;

    public Graph() {
        nodes = new HashMap<>();
    }

    public boolean isEmpty() {
        return nodes.size() <= 0;
    }

    public Set<T> getNodeSet() {
        return nodes.keySet();
    }

    public void addNode(T node) {
        if (nodes.get(node) != null)
            return;
        nodes.put(node, new ArrayList<>());
    }

    public boolean containsNode(T node) {
        return nodes.get(node) != null;
    }

    public void addEdge(T a, T b) {
        nodes.computeIfAbsent(a, k -> new ArrayList<>());
        nodes.computeIfAbsent(b, k -> new ArrayList<>());

        List<T> aEdgeList = nodes.get(a);
        List<T> bEdgeList = nodes.get(b);

        if (!aEdgeList.contains(b)) {
            aEdgeList.add(b);
            bEdgeList.add(a);
        }
    }

    public void addEdges(T a, List<T> b) {
        if (nodes.get(a) == null)
            return;

        for (T dest : b) {
            if (nodes.get(dest) == null)
                throw new RuntimeException("Graph::addEdges: Destination not present in graph");
            List<T> destEdgeList = nodes.get(dest);
            destEdgeList.add(a);
        }

        nodes.put(a, b);
    }

    public void removeEdge(T a, T b) {
        if (nodes.get(a) == null || nodes.get(b) == null)
            throw new RuntimeException("Graph::removeEdge: either a or b does not exist in the graph");

        List<T> aEdgeList = nodes.get(a);
        List<T> bEdgeList = nodes.get(b);

        aEdgeList.remove(b);
        bEdgeList.remove(a);
    }

    public int degree(T a) {
        if (nodes.get(a) == null)
            return -1;
        return nodes.get(a).size();
    }

    public List<T> removeNode(T a) {
        if (nodes.get(a) == null)
            throw new RuntimeException("Graph::removeNode: a does not exist in the graph");

        // remove all references to the node from the graph
        List<T> edgeList = nodes.get(a);

        // remove the node
        nodes.remove(a);

        for (T b : edgeList) {
            if (nodes.get(b) != null) {
                nodes.get(b).remove(a);
            }
        }

        // return a list of the nodes that the node 'a' was connected to
        return edgeList;
    }


    public List<List<T>> color(int colors) {
        return null;
    }

}
