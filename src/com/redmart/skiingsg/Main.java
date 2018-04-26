package com.redmart.skiingsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main
{
    class Node
    {
        int value;
        int row;
        int col;

        LinkedList<Node> neighbours = new LinkedList<>();
        boolean isSource = false;

        Node(int value, int row, int col)
        {
            this.value = value;
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString()
        {
            return String.format("%d", value);
        }

        String print()
        {
            return String.format(
                    "{%d, isSource: %b, \tNeighours: %s}",
                    value, isSource, neighbours
            );
        }
    }

    class Route
    {
        int steep;
        LinkedList<Node> nodes;

        Route(int steep, LinkedList<Node> nodes)
        {
            this.nodes = nodes;
            this.steep = steep;
        }

        int size()
        {
            return nodes.size();
        }

        Stream<Node> stream()
        {
            return nodes.stream();
        }
    }

    private static List<Route> routes = new LinkedList<>();

    private static final boolean COLLECT_ALL_LONGEST_ROUTES = false;

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("usage: java -jar Main.jar <mapfile>");
            return;
        }

        Path mapFile = Paths.get(args[0]);

        Main main = new Main();

        Node[][] graph = {};

        try
        {
            graph = main.readMapFile(mapFile);
            main.addNeighbours(graph);
        }
        catch (IOException e)
        {
            System.out.printf("error processing file '%s':\n\n", mapFile);
            e.printStackTrace();
            return;
        }

        main.dfsRecursiveAL(graph);

        print(routes);
    }

    private void dfsRecursiveAL(Node[][] graph)
    {
        routes.clear();

        List<Node> visited = new ArrayList<>(graph.length);
        Map<Node, Node> parents = new LinkedHashMap<>();

        for (Node[] nodes : graph)
        {
            for (Node node : nodes)
            {
                if (!node.isSource)
                { continue; }

                visited.clear();
                parents.clear();

                visitRecursiveAL(node, parents, visited);
                collectRoutes(parents);
            }
        }
    }

    private void visitRecursiveAL(Node node, Map<Node, Node> parents, List<Node> visited)
    {
        visited.add(node);

        for (Node n : node.neighbours)
        {
            if (visited.contains(n))
            { continue; }

            parents.put(n, node);
            visitRecursiveAL(n, parents, visited);
        }
    }

    private void collectRoutes(Map<Node, Node> parents)
    {
        for (Map.Entry<Node, Node> entry : parents.entrySet())
        {
            LinkedList<Node> route = new LinkedList<>();
            route.add(entry.getKey());
            collectRoutes(entry.getKey(), parents, route);
        }
    }

    private void collectRoutes(Node node, Map<Node, Node> parents, LinkedList<Node> route)
    {
        Node parent = parents.get(node);

        if (parent != null)
        {
            route.add(parent);
            collectRoutes(parent, parents, route);
            return;
        }

        if (routes.size() == 0)
        {
            routes.add(new Route(steep(route), route));
            return;
        }

        if (route.size() > routes.get(0).size())
        {
            routes.clear();
            routes.add(new Route(steep(route), route));
            return;
        }

        if (route.size() == routes.get(0).size())
        {
            int newSteep = steep(route);
            if (!COLLECT_ALL_LONGEST_ROUTES)
            {
                int currentSteep = routes.get(0).steep;

                if (newSteep < currentSteep) { return; }
                if (newSteep > currentSteep) { routes.clear(); }
            }
            routes.add(new Route(newSteep, route));
        }
    }

    private int steep(LinkedList<Node> route)
    {
        if (route == null || route.size() == 0)
        {
            return 0;
        }

        return route.get(route.size() - 1).value - route.get(0).value;
    }

    private Node[][] readMapFile(Path file) throws IOException
    {
        Node[][] graph = {};

        try (
                BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))
        )
        {
            boolean isFirstLine = true;
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null)
            {
                String[] splitted = line.split(" ");

                if (isFirstLine)
                {
                    graph = new Node[Integer.valueOf(splitted[0])][Integer.valueOf(splitted[1])];
                    isFirstLine = false;
                    continue;
                }

                int col = 0;
                for (String s : splitted)
                {
                    graph[row][col] = new Node(Integer.valueOf(s), row, col);
                    col++;
                }

                row++;
            }
        }

        return graph;
    }

    private void addNeighbours(Node[][] graph)
    {
        for (Node[] nodes : graph)
        {
            for (Node node : nodes)
            {
                LinkedList<Node> neighbors = new LinkedList<>();
                boolean isSource = true;

                boolean west = node.col - 1 >= 0;
                if (west)
                {
                    if (graph[node.row][node.col - 1].value < node.value)
                    { neighbors.add(graph[node.row][node.col - 1]); }
                    else
                    { isSource = false; }
                }

                boolean south = node.row + 1 < graph.length;
                if (south)
                {
                    if (graph[node.row + 1][node.col].value < node.value)
                    { neighbors.add(graph[node.row + 1][node.col]); }
                    else
                    { isSource = false; }
                }

                boolean east = node.col + 1 < graph[node.row].length;
                if (east)
                {
                    if (graph[node.row][node.col + 1].value < node.value)
                    { neighbors.add(graph[node.row][node.col + 1]); }
                    else
                    { isSource = false; }
                }

                boolean north = node.row - 1 >= 0;
                if (north)
                {
                    if (graph[node.row - 1][node.col].value < node.value)
                    { neighbors.add(graph[node.row - 1][node.col]); }
                    else
                    { isSource = false; }
                }

                node.neighbours = neighbors;
                node.isSource = isSource;
            }
        }
    }

    private static void print(List<Route> routes)
    {
        System.out.printf(
                "\nfound %d route(s) with length %d:\n\n",
                routes.size(), routes.get(0).size()
        );
        for (Route route : routes)
        {
            System.out.printf(
                    "steep %d:\t%s\n",
                    route.steep,
                    route.stream()
                            .map(n -> n.value)
                            .sorted(Collections.reverseOrder())
                            .map(Object::toString)
                            .collect(Collectors.joining(" -> "))
            );
        }
        System.out.println();
    }
}
