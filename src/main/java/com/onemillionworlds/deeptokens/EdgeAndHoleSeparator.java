package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Edges are the outermost pixels of a token. Holes are the innermost pixels of a token. This
 * seperates the two based on the winding of the perimeter.
 */
public class EdgeAndHoleSeparator{

    public static EdgesAndHoles separatePerimeters(List<List<Point>> perimeters) {
        List<List<Point>> edges = new ArrayList<>();
        List<List<Point>> holes = new ArrayList<>();
        for (List<Point> perimeter : perimeters) {
            if (!isClockwise(perimeter)) {
                edges.add(perimeter);
            } else {
                holes.add(perimeter);
            }
        }
        return new EdgesAndHoles(edges, holes);
    }

    private static boolean isClockwise(List<Point> points) {
        double sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Point current = points.get(i);
            Point next = points.get((i + 1) % points.size());
            sum += (next.x - current.x) * (next.y + current.y);
        }
        return sum > 0;
    }

    public record EdgesAndHoles(List<List<Point>> edges, List<List<Point>> holes) {}

}
