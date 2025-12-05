package com.onemillionworlds.deeptokens;

import com.jme3.math.Vector2f;
import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This takes a list of outer edges and inner holes and maps them to each other.
 * An edge may contain multiple holes, or none.
 * <p>
 * A hole may itself contain islands but we don't directly pair those up, what we do do is ensure that the islands holes
 * are not paired with the larger outer edge, only with the edge that DIRECTLY contains them.
 */
public class EdgeHoleMapper {

    public static List<EdgeWithContainedHoles> mapHolesToEdges(List<List<PixelPosition>> edges, List<List<PixelPosition>> holes) {
        Map<List<PixelPosition>, List<List<PixelPosition>>> edgeToHolesMap = new HashMap<>();

        for(List<PixelPosition> edge : edges){
            edgeToHolesMap.put(edge, new ArrayList<>());
        }

        for (List<PixelPosition> hole : holes) {
            List <PixelPosition> smallestContainingEdge = null;
            double smallestArea = Double.MAX_VALUE;

            for (List<PixelPosition> edge : edges) {
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
        for (Map.Entry<List<PixelPosition>, List<List<PixelPosition>>> entry : edgeToHolesMap.entrySet()) {
            edgeWithContainedHoles.add(new EdgeWithContainedHoles(entry.getKey(), entry.getValue()));
        }

        return edgeWithContainedHoles;
    }

    private static boolean isHoleInEdge(List<PixelPosition> hole, List<PixelPosition> edge) {
        for (PixelPosition holePoint : hole) {
            if (isPointInPolygon(holePoint, edge)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPointInPolygon(PixelPosition p, List<PixelPosition> polygon) {
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

    private static double calculatePolygonArea(List <PixelPosition> points) {
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

        List<PixelPosition> edge;
        List<List<PixelPosition>> holes;

        public EdgeWithContainedHoles(List<PixelPosition> edge, List<List<PixelPosition>> holes){
            this.edge = edge;
            this.holes = holes;
        }



        public List <PixelPosition> asSinglePerimeter() {
            List<PixelPosition> singlePerimeter = new ArrayList<>(edge);

            List<List<PixelPosition>> holes = new ArrayList<>(this.holes);

            while(!holes.isEmpty()){
                List<PixelPosition> bestHoleToAdd = holes.stream()
                        .min(Comparator.comparingDouble(hole -> findClosestPoints(singlePerimeter, hole).distance()))
                        .orElseThrow(() -> new RuntimeException("Suprising no holes"));

                holes.remove(bestHoleToAdd);
                ClosestPoints closestPoints = findClosestPoints(singlePerimeter, bestHoleToAdd);
                int edgeInsertIndex = singlePerimeter.indexOf(closestPoints.edgePoint());
                List <PixelPosition> connection = createConnection(closestPoints, bestHoleToAdd);
                singlePerimeter.addAll(edgeInsertIndex + 1, connection);
            }

            return singlePerimeter;
        }

        /**
         * This helps debug problems with the algorithm, but showing the total edge is a slowly changing
         * colour so you can see the order of the edges being added.
         * @return
         */
        public BufferedImage completeLineDebugImage(){
            List <PixelPosition> perimeter = asSinglePerimeter();
            int maxX = perimeter.stream().mapToInt(p -> p.x).max().getAsInt()+20;
            int targetMaxX = 1000;

            float multiplier = Math.max(targetMaxX / (float)maxX, 1);
            perimeter = perimeter.stream().map(
                    p -> new PixelPosition((int)(p.x * multiplier)+20, (int)(p.y * multiplier)+20)
                ).collect(Collectors.toList());

            maxX = perimeter.stream().mapToInt(p -> p.x).max().getAsInt()+20;
            int maxY = perimeter.stream().mapToInt(p -> p.y).max().getAsInt()+20;


            BufferedImage image = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2D = (Graphics2D)image.getGraphics();
            graphics2D.setBackground(Color.BLACK);
            graphics2D.setStroke(new BasicStroke(3));
            for(int i=0;i<perimeter.size();i++){
                PixelPosition lastPoint = perimeter.get( i==0 ? perimeter.size()-1 : (i-1));
                PixelPosition point = perimeter.get(i);
                PixelPosition nextPoint = perimeter.get((i+1)%perimeter.size());
                float intensitity = 0.25f + 0.75f * (i/(float)perimeter.size());

                Vector2f backVector = new Vector2f(point.x-lastPoint.x, point.y- lastPoint.y ).normalizeLocal();
                Vector2f forwardVector = new Vector2f(nextPoint.x - point.x, nextPoint.y - point.y).normalizeLocal();

                Vector2f averageSidewaysDirection = new Vector2f();
                averageSidewaysDirection.addLocal(rotate90(backVector));
                averageSidewaysDirection.addLocal(rotate90(forwardVector));
                averageSidewaysDirection.multLocal(0.5f);

                float distanceToBringIn = 15;

                Vector2f insideOfCorner = new Vector2f(point.x, point.y).addLocal(averageSidewaysDirection.mult(distanceToBringIn));

                graphics2D.setColor(new Color(intensitity, intensitity, intensitity));

                graphics2D.drawLine(point.x, point.y, nextPoint.x, nextPoint.y);
                graphics2D.setColor(Color.WHITE);
                //numbers go inside the shape (beware zero sized cuts)
                graphics2D.drawString(""+i, insideOfCorner.x, insideOfCorner.y+5); //the offset is for the font size
            }
            graphics2D.dispose();
            return image;
        }

        private Vector2f rotate90(Vector2f in){
            return new Vector2f(-in.y, in.x);
        }

        private ClosestPoints findClosestPoints(List<PixelPosition> edge, List<PixelPosition> hole) {
            double minDistance = Double.MAX_VALUE;
            PixelPosition closestEdgePoint = null;
            PixelPosition closestHolePoint = null;

            for (int i=0;i<edge.size();i++) {
                PixelPosition edgePoint = edge.get(i);
                for (int j =0;j<hole.size();j++) {
                    PixelPosition holePoint = hole.get(j);
                    double distance = edgePoint.distance(holePoint);
                    if (distance < minDistance) {
                        //check if the line made between the two points is "facing the right way" for the break to be valid.
                        //this is only really relevant if the point has already been part of an edge insertion and so duplicated, one will head off in one
                        //direction and the other will head off in the other direction, must choose the correct one of the
                        //duplicate pair
                        if (isBDInArc(holePoint, edge, i)){
                            continue;
                        }

                        if (isBDInArc(edgePoint, hole, j)){
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

        private List <PixelPosition> createConnection(ClosestPoints closestPoints, List <PixelPosition> hole) {

            PixelPosition edgePoint = closestPoints.edgePoint();
            PixelPosition holePoint = closestPoints.holePoint();

            // Rearrange the hole points so that the hole starts and ends at the closest hole point
            int holeStartIndex = hole.indexOf(holePoint);
            List <PixelPosition> rearrangedHole = new ArrayList<>();
            for (int i = holeStartIndex; i < hole.size(); i++) {
                rearrangedHole.add(hole.get(i));
            }
            for (int i = 0; i < holeStartIndex; i++) {
                rearrangedHole.add(hole.get(i));
            }
            List <PixelPosition> connection = new ArrayList<>(rearrangedHole.size() + 2);
            connection.addAll(rearrangedHole);
            connection.add(holePoint);
            connection.add(edgePoint);

            return connection;
        }

        private static class ClosestPoints{
            PixelPosition edgePoint;
            PixelPosition holePoint;

            public ClosestPoints(PixelPosition edgePoint, PixelPosition holePoint){
                this.edgePoint = edgePoint;
                this.holePoint = holePoint;
            }

            public PixelPosition edgePoint(){
                return edgePoint;
            }

            public PixelPosition holePoint(){
                return holePoint;
            }

            public double distance(){
                return edgePoint.distance(holePoint);
            }
        }

        private static boolean isBDInArc(PixelPosition point, List <PixelPosition> edge, int indexOfCentrePointOfLine){
            int before = indexOfCentrePointOfLine - 1;
            if (before < 0){
                before+= edge.size();
            }
            int after = indexOfCentrePointOfLine + 1;
            if (after >= edge.size()){
                after -= edge.size();
            }

            PixelPosition a = edge.get(before);
            PixelPosition b = edge.get(indexOfCentrePointOfLine);
            PixelPosition c = edge.get(after);
            return isBDInArc(a, b, c, point);
        }

        public static boolean isBDInArc(PixelPosition A, PixelPosition B, PixelPosition C, PixelPosition D) {

            double angleA = Math.atan2(A.y - B.y, A.x - B.x);
            double angleC = Math.atan2(C.y - B.y, C.x - B.x);
            double angleD = Math.atan2(D.y - B.y, D.x - B.x);

            //want angleD to be between angleA and angleC (but may have to take account of wrapping around)
            if (angleC<angleA){
                angleC += Math.PI*2;
            }
            if (angleD<angleA){
                angleD += Math.PI*2;
            }

            return angleA < angleD && angleD < angleC;
        }

        private static int crossProduct(PixelPosition v1, PixelPosition v2) {
            return v1.x * v2.y - v1.y * v2.x;
        }

        private static boolean pointIsToLeftOfLine(PixelPosition point, PixelPosition a, PixelPosition b,  PixelPosition c){
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