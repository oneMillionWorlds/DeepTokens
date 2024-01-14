package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgeRasterizer{

    public static List<Point> rasterizeEdge(List<Point> edgePoints) {
        List<Point> rasterizedPoints = new ArrayList<>();

        for (int i = 0; i < edgePoints.size() - 1; i++) {
            rasterizedPoints.addAll(generateLine(edgePoints.get(i), edgePoints.get(i + 1)));
        }

        return rasterizedPoints;
    }

    public static Set<Point> thickenLine(List<Point> linePoints, int width) {
        Set<Point> thickenedPoints = new HashSet<>();
        int d = width;

        for (Point p : linePoints) {
            for (int dx = -d; dx <= d; dx++) {
                for (int dy = -d; dy <= d; dy++) {
                    thickenedPoints.add(new Point(p.x + dx, p.y + dy));
                }
            }
        }

        return thickenedPoints;
    }

    private static List<Point> generateLine(Point p1, Point p2) {
        List<Point> linePoints = new ArrayList<>();

        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x;
        int y2 = p2.y;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            linePoints.add(new Point(x1, y1));

            if (x1 == x2 && y1 == y2) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }

        return linePoints;
    }

}
