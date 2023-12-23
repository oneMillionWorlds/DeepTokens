package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DouglasPeuckerLineSimplifier{


    public static List<Point> simplify(List<Point> points, double epsilon) {
        if (points.size() < 3) return points;

        int index = -1;
        double maxDistance = 0.0;
        for (int i = 1; i < points.size() - 1; i++) {
            double distance = distanceToLine(points.get(i),points.get(0), points.get(points.size() - 1));
            if (distance > maxDistance) {
                index = i;
                maxDistance = distance;
            }
        }

        if (maxDistance > epsilon) {
            List<Point> leftList = simplify(points.subList(0, index + 1), epsilon);
            List<Point> rightList = simplify(points.subList(index, points.size()), epsilon);

            List<Point> result = new ArrayList<>(leftList);
            result.remove(result.size() - 1); // remove the duplicate point
            result.addAll(rightList);
            return result;
        } else {
            List<Point> result = new ArrayList<>();
            result.add(points.get(0));
            result.add(points.get(points.size() - 1));
            return result;
        }
    }

    static double distanceToLine(Point testPoint, Point lineStart, Point lineEnd) {
        int x = testPoint.x;
        int y = testPoint.y;
        double normalLength = Math.hypot(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y);
        return Math.abs((x - lineStart.x) * (lineEnd.y - lineStart.y) - (y - lineStart.y) * (lineEnd.x - lineStart.x)) / normalLength;
    }
}
