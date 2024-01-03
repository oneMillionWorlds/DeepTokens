package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Triangulariser{

    private static final Logger LOGGER = Logger.getLogger(Triangulariser.class.getName());

    public static List<Triangle> triangulate(List<Point> perimeter) {
        List<Triangle> triangles = new ArrayList<>();
        List<Point> remainingPoints = new ArrayList<>(perimeter);

        boolean firstTriangulationFailure = true;
        int problemLevel = 0;

        while (remainingPoints.size() > 3) {
            int size = remainingPoints.size();
            for (int i = 0; i < size; i++) {
                Point prev = remainingPoints.get((i + size - 1) % size);
                Point curr = remainingPoints.get(i);
                Point next = remainingPoints.get((i + 1) % size);

                if(problemLevel ==0 && isConvex(prev, curr, next) && noPointsInside(remainingPoints, prev, curr, next)){
                    triangles.add(new Triangle(prev, curr, next));
                    remainingPoints.remove(i);
                    break;
                }
                if (problemLevel == 1 && (isConvex(prev, curr, next) && noPointsInsideDuplicateRelaxed(remainingPoints, prev, curr, next))) {
                    triangles.add(new Triangle(prev, curr, next));
                    remainingPoints.remove(i);
                    problemLevel = 0;
                    break;
                }
                if (problemLevel == 2) {
                    //just start forming random triangles
                    triangles.add(new Triangle(prev, curr, next));
                    remainingPoints.remove(i);
                    problemLevel = 0;
                    break;
                }
            }
            int afterLoopSize = remainingPoints.size();
            if (size == afterLoopSize) {
                problemLevel++;
                if (firstTriangulationFailure){
                    firstTriangulationFailure = false;
                    LOGGER.warning("Triangulation failure (will guess triangles). Points were: \n" + TriangularisationFailureException.pointsToString(remainingPoints));
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
            if (p !=a && p!=b && p!=c && isPointInsideTriangle(p, a, b, c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean noPointsInsideDuplicateRelaxed(List<Point> points, Point a, Point b, Point c) {
        for (Point p : points) {
            if (!p.equals(a) && !p.equals(b) && !p.equals(c) && isPointInsideTriangle(p, a, b, c)) {
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

    /**
     * To generate the holes we create extra points equal to the other points. The triangluration algorithm needs
     * to be told not to worry about these points and consider them "outside" the triangle being built
     */
    private static boolean isPointVertexOfTriangle(Point p, Point a, Point b, Point c) {
        return p.equals(a) || p.equals(b) || p.equals(c);
    }

    public static class TriangularisationFailureException extends RuntimeException {
        List<Point> points;
        public TriangularisationFailureException(String message, List<Point> points) {
            super(message + ". Do you have single pixel wide regions? Problematic points were (remember y may be flipped): \n. " + pointsToString(points));
            this.points = points;
        }

        public List<Point> getPoints(){
            return points;
        }

        public String getPointsAsString(){
            return pointsToString(points);
        }

        public static String pointsToString(List<Point> points){
            StringBuilder sb = new StringBuilder();
            for(Point p:points){
                sb.append("(").append(p.x).append(",").append(p.y).append("), ");
            }
            return sb.toString();
        }
    }

}
