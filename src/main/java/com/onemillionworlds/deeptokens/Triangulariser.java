package com.onemillionworlds.deeptokens;

import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import nl.bartlouwers.earcut4j.Earcut;

public class Triangulariser{

    private static final Logger LOGGER = Logger.getLogger(Triangulariser.class.getName());

    public static List<Triangle> triangulate(List <PixelPosition> perimeter, boolean strictMode) {
        if (perimeter == null || perimeter.size() < 3) {
            return List.of();
        }

        double[] data = new double[perimeter.size() * 2];
        for (int i = 0; i < perimeter.size(); i++) {
            PixelPosition p = perimeter.get(i);
            data[i * 2] = p.x;
            data[i * 2 + 1] = p.y;
        }

        List<Integer> triangleData;
        try {
            triangleData = Earcut.earcut(data, null, 2);
        } catch (Exception e) {
            if (strictMode) {
                throw new TriangulationFailureException(
                        "Triangulation failure (earcut exception). Points were: \n" + pointsToString(perimeter),
                        perimeter
                );
            } else {
                LOGGER.warning("Triangulation failure (earcut exception). Will guess triangles. Points were: \n" + pointsToString(perimeter));
                triangleData = List.of();
            }
        }

        if (triangleData.isEmpty()) {
            if (strictMode) {
                throw new TriangulationFailureException(
                        "Triangulation failure (no triangles from earcut). Points were: \n" + pointsToString(perimeter),
                        perimeter
                );
            } else {
                LOGGER.warning("Triangulation failure (no triangles from earcut). Will guess triangles. Points were: \n" + pointsToString(perimeter));
                // Fallback: triangle fan from the first vertex
                List<Triangle> fan = new ArrayList<>();
                for (int i = 1; i < perimeter.size() - 1; i++) {
                    fan.add(new Triangle(perimeter.get(0), perimeter.get(i), perimeter.get(i + 1)));
                }
                return fan;
            }
        }

        List<Triangle> triangles = new ArrayList<>(triangleData.size() / 3);
        for (int i = 0; i < triangleData.size(); i += 3) {
            triangles.add(new Triangle(
                    perimeter.get(triangleData.get(i)),
                    perimeter.get(triangleData.get(i + 1)),
                    perimeter.get(triangleData.get(i + 2))
            ));
        }
        return triangles;
    }

    public static String pointsToString(List <PixelPosition> points){
        StringBuilder sb = new StringBuilder();
        for(PixelPosition p:points){
            sb.append("(").append(p.x).append(",").append(p.y).append("), ");
        }
        return sb.toString();
    }

    public static class TriangulationFailureException extends RuntimeException{

        List <PixelPosition> remainingPoints;
        public TriangulationFailureException(String message, List <PixelPosition> remainingPoints){
            super(message);
            this.remainingPoints = remainingPoints;
        }

        public List <PixelPosition> getRemainingPoints(){
            return remainingPoints;
        }
    }
}
