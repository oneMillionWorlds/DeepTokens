package com.onemillionworlds.deeptokens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This gives the image an expanded "fuzzy edge". This is useful in case the simplification process leads to the shape
 * being slightly larger and going off the edge of the original image.
 */
public class ImageEdgeExpander{

    /**
     *
     * @param originalImage the raw image
     * @param maximumEdgeEpsilonError furthest the edge can be from the original edge (simplification episilon)
     * @param averagingDistance how far to look for pixels to average when on edge (may be more than maximumEdgeEpsilonError to give dirty edge reduction)
     * @param edges the edges
     * @return
     */
    public static BufferedImage processImage(BufferedImage originalImage, int maximumEdgeEpsilonError, int averagingDistance, List<List<Point>> edges) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = newImage.getGraphics();
        graphics.drawImage(originalImage, 0, 0, null);

        int[] pixels = originalImage.getRGB(0, 0, width, height, null, 0, width);


        Set<Point> pointsOnEdge = new HashSet<>();
        for(List<Point> edge : edges){
            pointsOnEdge.addAll(EdgeRasterizer.thickenLine(EdgeRasterizer.rasterizeEdge(edge), maximumEdgeEpsilonError));
        }

        for(Point pointNearEdge : pointsOnEdge){
            if (pointNearEdge.x>=0 && pointNearEdge.y>=0 && pointNearEdge.x<width && pointNearEdge.y<height){
                Color averageColor = getAverageColorAround(pixels, pointNearEdge.x, pointNearEdge.y, width, height, averagingDistance);
                if(averageColor != null){
                    newImage.setRGB(pointNearEdge.x, pointNearEdge.y, averageColor.getRGB());
                }
            }
        }

        return newImage;
    }

    private static Color getAverageColorAround(int[] pixels, int x, int y, int width, int height, int distance) {
        int redTotal = 0, greenTotal = 0, blueTotal = 0, count = 0;

        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                    int pixel = pixels[nx + ny*width];
                    int alpha = (pixel >> 24) & 0xff;

                    if (alpha != 0) { // Not transparent
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = (pixel) & 0xff;

                        redTotal += red;
                        greenTotal += green;
                        blueTotal += blue;
                        count++;
                    }
                }
            }
        }

        return count > 0 ? new Color(redTotal / count, greenTotal / count, blueTotal / count) : null;
    }

}
