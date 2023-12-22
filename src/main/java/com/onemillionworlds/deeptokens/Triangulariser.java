package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Triangulariser{

    public static List<Triangle> triangulate(List<Point> perimeter) {
        List<Triangle> triangles = new ArrayList<>();
        List<Point> remainingPoints = new ArrayList<>(perimeter);

        while (remainingPoints.size() > 3) {
            int size = remainingPoints.size();
            for (int i = 0; i < size; i++) {
                Point prev = remainingPoints.get((i + size - 1) % size);
                Point curr = remainingPoints.get(i);
                Point next = remainingPoints.get((i + 1) % size);

                if (isConvex(prev, curr, next) && noPointsInside(remainingPoints, prev, curr, next)) {
                    List<Point> triangle = new ArrayList<>();
                    triangles.add(new Triangle(prev, curr, next));

                    remainingPoints.remove(i);
                    break;
                }
            }
        }

        // Add the last remaining triangle
        triangles.add(new Triangle(remainingPoints.get(0), remainingPoints.get(1), remainingPoints.get(2)));

        return triangles;
    }

    private static boolean isConvex(Point a, Point b, Point c) {
        return ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) >= 0;
    }

    private static boolean noPointsInside(List<Point> points, Point a, Point b, Point c) {
        for (Point p : points) {
            if (p != a && p != b && p != c && isPointInsideTriangle(p, a, b, c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPointInsideTriangle(Point p, Point a, Point b, Point c) {
        int ab = (a.x - p.x) * (b.y - a.y) - (a.y - p.y) * (b.x - a.x);
        int bc = (b.x - p.x) * (c.y - b.y) - (b.y - p.y) * (c.x - b.x);
        int ca = (c.x - p.x) * (a.y - c.y) - (c.y - p.y) * (a.x - c.x);
        return (ab >= 0 && bc >= 0 && ca >= 0) || (ab <= 0 && bc <= 0 && ca <= 0);
    }

}
