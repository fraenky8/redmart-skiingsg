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
        }
        catch (IOException e)
        {
            System.out.printf("error processing file '%s':\n\n", mapFile);
            e.printStackTrace();
        }

        main.dfs(graph);

        System.out.printf(
            "\nfound %d route(s) with length %d and steep %d:\n\n",
            routes.size(), routes.get(0).size(), routes.get(0).steep
        );
        for (Route route : routes)
        {
            System.out.print(
                route.stream()
                    .map(n -> n.value)
                    .sorted(Collections.reverseOrder())
                    .map(Object::toString)
                    .collect(Collectors.joining(" -> "))
            );
        }
        System.out.println();
    }

    private void dfs(Node[][] graph)
    {
        for (Node[] nodes : graph)
        {
            for (Node node : nodes)
            {
                Map<Node, Node> parents = new HashMap<>();
                visit(node, graph, parents);
                findRoutes(parents);
            }
        }
    }

    private void visit(Node node, Node[][] graph, Map<Node, Node> parents)
    {
        boolean north = node.row - 1 >= 0 && graph[node.row - 1][node.col].value < node.value;
        if (north)
        {
            parents.put(graph[node.row - 1][node.col], node);
            visit(graph[node.row - 1][node.col], graph, parents);
        }

        boolean east = node.col + 1 < graph[node.row].length && graph[node.row][node.col + 1].value < node.value;
        if (east)
        {
            parents.put(graph[node.row][node.col + 1], node);
            visit(graph[node.row][node.col + 1], graph, parents);
        }

        boolean south = node.row + 1 < graph.length && graph[node.row + 1][node.col].value < node.value;
        if (south)
        {
            parents.put(graph[node.row + 1][node.col], node);
            visit(graph[node.row + 1][node.col], graph, parents);
        }

        boolean west = node.col - 1 >= 0 && graph[node.row][node.col - 1].value < node.value;
        if (west)
        {
            parents.put(graph[node.row][node.col - 1], node);
            visit(graph[node.row][node.col - 1], graph, parents);
        }
    }

    private void findRoutes(Map<Node, Node> parents)
    {
        for (Map.Entry<Node, Node> entry : parents.entrySet())
        {
            LinkedList<Node> route = new LinkedList<>();
            route.add(entry.getKey());
            findRoutes(entry.getKey(), parents, route);
        }
    }

    private void findRoutes(Node node, Map<Node, Node> parents, LinkedList<Node> route)
    {
        Node parent = parents.get(node);

        if (parent != null)
        {
            route.add(parent);
            findRoutes(parent, parents, route);
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
            int currentSteep = routes.get(0).steep;

            if (newSteep < currentSteep) { return; }
            if (newSteep > currentSteep) { routes.clear(); }
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
}
