package com.onemillionworlds.deeptokens;

import com.jme3.math.Vector2f;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
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

            List<List<Point>> holes = new ArrayList<>(this.holes);

            while(!holes.isEmpty()){
                List<Point> bestHoleToAdd = holes.stream()
                        .min(Comparator.comparingDouble(hole -> findClosestPoints(singlePerimeter, hole).distance()))
                        .orElseThrow(() -> new RuntimeException("Suprising no holes"));

                holes.remove(bestHoleToAdd);
                ClosestPoints closestPoints = findClosestPoints(singlePerimeter, bestHoleToAdd);
                int edgeInsertIndex = singlePerimeter.indexOf(closestPoints.edgePoint());
                List<Point> connection = createConnection(closestPoints, bestHoleToAdd);
                singlePerimeter.addAll(edgeInsertIndex + 1, connection);
            }

            return singlePerimeter;
        }

        private ClosestPoints findClosestPoints(List<Point> edge, List<Point> hole) {
            double minDistance = Double.MAX_VALUE;
            Point closestEdgePoint = null;
            Point closestHolePoint = null;

            for (int i=0;i<edge.size();i++) {
                Point edgePoint = edge.get(i);
                for (int j =0;j<hole.size();j++) {
                    Point holePoint = hole.get(j);
                    double distance = edgePoint.distance(holePoint);
                    if (distance < minDistance) {
                        //check if the line made between the two points is "facing the right way" for the break to be valid.
                        //this is only really relevant if the point has already been part of an edge insertion and so duplicated, one will head off in one
                        //direction and the other will head off in the other direction, must choose the correct one of the
                        //duplicate pair

                        if (!pointIsToLeftOfLine(holePoint, edge, i)){
                            continue;
                        }

                        if (!pointIsToLeftOfLine(edgePoint, hole, j)){
                            continue;
                        }

                        minDistance = distance;
                        closestEdgePoint = edgePoint;
                        closestHolePoint = holePoint;
                    }
                }
            }

            return new ClosestPoints(closestEdgePoint, closestHolePoint);
        }

        private List<Point> createConnection(ClosestPoints closestPoints, List<Point> hole) {

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
            List<Point> connection = new ArrayList<>(rearrangedHole.size() + 2);
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

            public double distance(){
                return edgePoint.distance(holePoint);
            }
        }

        private static boolean pointIsToLeftOfLine(Point point, List<Point> edge, int indexOfCentrePointOfLine){
            int before = indexOfCentrePointOfLine - 1;
            if (before < 0){
                before+= edge.size();
            }
            int after = indexOfCentrePointOfLine + 1;
            if (after >= edge.size()){
                after -= edge.size();
            }

            Point a = edge.get(before);
            Point b = edge.get(indexOfCentrePointOfLine);
            Point c = edge.get(after);

            return pointIsToLeftOfLine(point, a, b, c);
        }

        private static boolean pointIsToLeftOfLine(Point point, Point a, Point b,  Point c){
            double determinantAB = (b.x - a.x) * (point.y - a.y) - (b.y - a.y) * (point.x - a.x);
            double determinantBC = (c.x - b.x) * (point.y - b.y) - (c.y - b.y) * (point.x - b.x);

            boolean isLeftAB = determinantAB > 0;
            boolean isLeftBC = determinantBC > 0;

            if (isLeftAB == isLeftBC) {
                // to the same side of both lines (if made infinite) no need to get complicated
                return isLeftAB;
            }  else {
                // in this case we imagine that the line extends from point B infinitely in two directions, towards A and towards C.
                // we project the point onto that line and see if it's projection is on the A <-> B bit or the B <-> C bit

                boolean aToBWins = projectOntoLine(new Vector2f(point.x, point.y), new Vector2f(a.x - b.x, a.y - b.y), new Vector2f(a.x, a.y)) > 0;

                return aToBWins ? isLeftAB : isLeftBC;
            }
        }

        /**
         * Projects the point onto the line and returns the distance along the line from the start of the line to the projection.
         * If the projection is before the start of the line then a negative value is returned.
         */
        private static double projectOntoLine(Vector2f lineStart, Vector2f lineDirection, Vector2f point){
            // Convert lineStart and point to vectors
            Vector2f startToPoint = new Vector2f(point.x - lineStart.x, point.y - lineStart.y);

            // This is the distance from lineStart to the projection along the line
            return startToPoint.dot(lineDirection);
        }

    }
}