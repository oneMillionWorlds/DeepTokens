package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This takes a list of outer edges and inner holes and maps them to each other.
 * An edge may contain multiple holes, or none.
 * <p>
 * A hole may itself contain islands but we don't directly pair those up, what we do do is ensure that the islands holes
 * are not paired with the larger outer edge, only with the edge that DIRECTLY contains them.
 */
public class EdgeHoleMapper {

    public static List<EdgeWithContainedHoles> mapHolesToEdges(List<List<Point>> edges, List<List<Point>> holes) {
        Map<List<Point>, List<List<Point>>> edgeToHolesMap = new HashMap<>();

        for(List<Point> edge : edges){
            edgeToHolesMap.put(edge, new ArrayList<>());
        }

        for (List<Point> hole : holes) {
            List<Point> smallestContainingEdge = null;
            double smallestArea = Double.MAX_VALUE;

            for (List<Point> edge : edges) {
                if (isHoleInEdge(hole, edge)) {
                    double area = calculatePolygonArea(edge);
                    if (area < smallestArea) {
                        smallestArea = area;
                        smallestContainingEdge = edge;
                    }
                }
            }

            if (smallestContainingEdge != null) {
                edgeToHolesMap.get(smallestContainingEdge).add(hole);
            }
        }

        List<EdgeWithContainedHoles> edgeWithContainedHoles = new ArrayList<>();
        for (Map.Entry<List<Point>, List<List<Point>>> entry : edgeToHolesMap.entrySet()) {
            edgeWithContainedHoles.add(new EdgeWithContainedHoles(entry.getKey(), entry.getValue()));
        }

        return edgeWithContainedHoles;
    }

    private static boolean isHoleInEdge(List<Point> hole, List<Point> edge) {
        for (Point holePoint : hole) {
            if (isPointInPolygon(holePoint, edge)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPointInPolygon(Point p, List<Point> polygon) {
        boolean result = false;
        int j = polygon.size() - 1;
        for (int i = 0; i < polygon.size(); i++) {
            if ((polygon.get(i).y > p.y) != (polygon.get(j).y > p.y) &&
                    (p.x < (polygon.get(j).x - polygon.get(i).x) * (p.y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(i).y) + polygon.get(i).x)) {
                result = !result;
            }
            j = i;
        }
        return result;
    }

    private static double calculatePolygonArea(List<Point> points) {
        double area = 0.0;
        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i++) {
            area += (points.get(j).x + points.get(i).x) * (points.get(j).y - points.get(i).y);
            j = i;
        }
        return Math.abs(area / 2.0);
    }

    /**
     * A single edge with its (directly contained) holes.
     */
    public static class EdgeWithContainedHoles{

        List<Point> edge;
        List<List<Point>> holes;

        public EdgeWithContainedHoles(List<Point> edge, List<List<Point>> holes){
            this.edge = edge;
            this.holes = holes;
        }



        public List<Point> asSinglePerimeter() {
            List<Point> singlePerimeter = new ArrayList<>(edge);

            for (List<Point> hole : holes) {
                // Find the closest points between edge and hole
                ClosestPoints closestPoints = findClosestPoints(singlePerimeter, hole);

                // Index in the edge where the connection will be inserted
                int edgeInsertIndex = singlePerimeter.indexOf(closestPoints.edgePoint());

                // Create the connection and splice it into the edge
                List<Point> connection = createConnection(closestPoints, hole);
                singlePerimeter.addAll(edgeInsertIndex + 1, connection);
            }

            return singlePerimeter;
        }

        private ClosestPoints findClosestPoints(List<Point> edge, List<Point> hole) {
            double minDistance = Double.MAX_VALUE;
            Point closestEdgePoint = null;
            Point closestHolePoint = null;

            for (Point edgePoint : edge) {
                for (Point holePoint : hole) {
                    double distance = edgePoint.distance(holePoint);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestEdgePoint = edgePoint;
                        closestHolePoint = holePoint;
                    }
                }
            }

            return new ClosestPoints(closestEdgePoint, closestHolePoint);
        }

        private List<Point> createConnection(ClosestPoints closestPoints, List<Point> hole) {
            List<Point> connection = new ArrayList<>();
            Point edgePoint = closestPoints.edgePoint();
            Point holePoint = closestPoints.holePoint();

            // Rearrange the hole points so that the hole starts and ends at the closest hole point
            int holeStartIndex = hole.indexOf(holePoint);
            List<Point> rearrangedHole = new ArrayList<>();
            for (int i = holeStartIndex; i < hole.size(); i++) {
                rearrangedHole.add(hole.get(i));
            }
            for (int i = 0; i < holeStartIndex; i++) {
                rearrangedHole.add(hole.get(i));
            }
            connection.addAll(rearrangedHole);
            connection.add(holePoint);
            connection.add(edgePoint);

            return connection;
        }

        private static class ClosestPoints{
            Point edgePoint;
            Point holePoint;

            public ClosestPoints(Point edgePoint, Point holePoint){
                this.edgePoint = edgePoint;
                this.holePoint = holePoint;
            }

            public Point edgePoint(){
                return edgePoint;
            }

            public Point holePoint(){
                return holePoint;
            }
        }

    }
}