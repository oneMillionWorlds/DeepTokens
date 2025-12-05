package com.onemillionworlds.deeptokens;

import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Edges are the outermost pixels of a token. Holes are the innermost pixels of a token. This
 * seperates the two based on the winding of the perimeter.
 */
public class EdgeAndHoleSeparator{

    public static EdgesAndHoles separatePerimeters(List<List<PixelPosition>> perimeters) {
        List<List<PixelPosition>> edges = new ArrayList<>();
        List<List<PixelPosition>> holes = new ArrayList<>();
        for (List<PixelPosition> perimeter : perimeters) {
            if (!isClockwise(perimeter)) {
                edges.add(perimeter);
            } else {
                holes.add(perimeter);
            }
        }
        return new EdgesAndHoles(edges, holes);
    }

    private static boolean isClockwise(List<PixelPosition> points) {
        double sum = 0;
        for (int i = 0; i < points.size(); i++) {
            PixelPosition current = points.get(i);
            PixelPosition next = points.get((i + 1) % points.size());
            sum += (next.x - current.x) * (next.y + current.y);
        }
        return sum > 0;
    }

    public static class EdgesAndHoles{
        List<List<PixelPosition>> edges;
        List<List<PixelPosition>> holes;

        public EdgesAndHoles(List<List<PixelPosition>> edges, List<List<PixelPosition>> holes){
            this.edges = edges;
            this.holes = holes;
        }

        public List<List<PixelPosition>> edges(){
            return edges;
        }

        public List<List<PixelPosition>> holes(){
            return holes;
        }
    }

}
