package com.onemillionworlds.deeptokens;

import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgeRasterizer{

    public static List <PixelPosition> rasterizeEdge(List <PixelPosition> edgePoints) {
        List <PixelPosition> rasterizedPoints = new ArrayList<>();

        for (int i = 0; i < edgePoints.size() - 1; i++) {
            rasterizedPoints.addAll(generateLine(edgePoints.get(i), edgePoints.get(i + 1)));
        }

        return rasterizedPoints;
    }

    public static Set <PixelPosition> thickenLine(List <PixelPosition> linePoints, int width) {
        Set <PixelPosition> thickenedPoints = new HashSet<>();
        int d = width;

        for (PixelPosition p : linePoints) {
            for (int dx = -d; dx <= d; dx++) {
                for (int dy = -d; dy <= d; dy++) {
                    thickenedPoints.add(new PixelPosition(p.x + dx, p.y + dy));
                }
            }
        }

        return thickenedPoints;
    }

    private static List <PixelPosition> generateLine(PixelPosition p1, PixelPosition p2) {
        List <PixelPosition> linePoints = new ArrayList<>();

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
            linePoints.add(new PixelPosition(x1, y1));

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
