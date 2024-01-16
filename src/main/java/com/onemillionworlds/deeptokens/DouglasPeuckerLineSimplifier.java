package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DouglasPeuckerLineSimplifier{


    public static List<List<Point>> simplifyAll(List<List<Point>> points, double epsilon) {
        List<List<Point>> simplifieds = new ArrayList<>();
        for (List<Point> list : points) {

            List<Point> simplified = simplify(list, epsilon);

            if (simplified.size()>3){ //get rid of tiny perimeters (probably forgotten pixels)
                simplifieds.add(simplified);
            }
        }
        return simplifieds;
    }

    public static List<Point> simplify(List<Point> points, double epsilon) {

        boolean[] keepPoints = new boolean[points.size()];
        keepPoints[0] = true;
        keepPoints[points.size() - 1] = true;

        simplifySection(points, 0, points.size() - 1, epsilon, keepPoints);

        List<Point> simplified = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (keepPoints[i]) {
                simplified.add(points.get(i));
            }
        }

        if(points.size() > 3){
            //the last and first points will be very close to each other. Probably we can get rid of one of them as well.
            double distanceIfRemoveFirst = distanceToLineApprox(simplified.get(0), simplified.get(1), simplified.get(simplified.size() - 1));

            if(distanceIfRemoveFirst < epsilon){
                simplified.remove(0);
            }

            double distanceIfRemoveLast = distanceToLineApprox(simplified.get(simplified.size() - 1), simplified.get(0), simplified.get(simplified.size() - 2));

            if(distanceIfRemoveLast < epsilon){
                simplified.remove(simplified.size() - 1);
            }
        }
        return simplified;
    }

    private static void simplifySection(List<Point> points, int start, int end, double epsilon, boolean[] keepPoints) {
        if (start + 1 == end) {
            return;
        }

        double maxDistance = 0;
        int index = start;
        for (int i = start + 1; i < end; i++) {
            double distance = distanceToLineApprox(points.get(i), points.get(start), points.get(end));
            if (distance > maxDistance) {
                index = i;
                maxDistance = distance;
            }
        }

        if (maxDistance > epsilon) {
            keepPoints[index] = true;
            simplifySection(points, start, index, epsilon, keepPoints);
            simplifySection(points, index, end, epsilon, keepPoints);
        }
    }


    static double distanceToLineApprox(Point testPoint, Point lineStart, Point lineEnd) {
        int x = testPoint.x;
        int y = testPoint.y;
        double normalLength = approximateHypot(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y);
        return Math.abs((x - lineStart.x) * (lineEnd.y - lineStart.y) - (y - lineStart.y) * (lineEnd.x - lineStart.x)) / normalLength;
    }

    /**
     * A faster (and more than accurate enough for our purposes) version of Math.hypot
     * @return
     */
    public static double approximateHypot(double x, double y){
        return approximateSquareRoot(x*x + y*y);
    }

    public static double approximateSquareRoot(double number) {
        return Double.longBitsToDouble( ( ( Double.doubleToLongBits( number )-(1L <<52) )>>1 ) + (1L <<61 ) );
    }

}
