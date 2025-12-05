package com.onemillionworlds.deeptokens;

import com.onemillionworlds.deeptokens.pixelprovider.BufferedImagePixelProvider;
import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;
import com.onemillionworlds.deeptokens.pixelprovider.PixelProvider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class MooreNeighbourhood {

    /**
     * Outputs the edges and holes of the given image. Note that the outer edges are wound anticlockwise, and the holes are wound clockwise.
     */
    static List<List<PixelPosition>> detectPerimeter(BufferedImage bufferedImage) {
        return detectPerimeter(new BufferedImagePixelProvider(bufferedImage));
    }

    /**
     * Outputs the edges and holes of the given image. Note that the outer edges are wound anticlockwise, and the holes are wound clockwise.
     */
    static List<List<PixelPosition>> detectPerimeter(PixelProvider pixels) {
        int width = pixels.getImageWidth();
        int height = pixels.getImageHeight();
        HashSet<PixelPosition> found = new HashSet<>();
        List<PixelPosition> list;
        List<List<PixelPosition>> lists = new ArrayList<>();
        boolean inside = false;

        List<Function<PixelPosition, PixelPosition>> neighborhood = List.of(
                point -> new PixelPosition(point.x - 1, point.y),
                point -> new PixelPosition(point.x - 1, point.y - 1),
                point -> new PixelPosition(point.x, point.y - 1),
                point -> new PixelPosition(point.x + 1, point.y - 1),
                point -> new PixelPosition(point.x + 1, point.y),
                point -> new PixelPosition(point.x + 1, point.y + 1),
                point -> new PixelPosition(point.x, point.y + 1),
                point -> new PixelPosition(point.x - 1, point.y + 1)
        );
        int[] checkLocationNrMap = new int[]{7, 7, 1, 1, 3, 3, 5, 5};

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                PixelPosition point = new PixelPosition(x, y);
                if (found.contains(point) && !inside) {
                    inside = true;
                    continue;
                }
                boolean isTransparent = pixels.isTransparent(x, y);
                if (!isTransparent && inside) {
                    continue;
                }
                if (isTransparent && inside) {
                    inside = false;
                    continue;
                }
                if (!isTransparent) {
                    list = new ArrayList<>();
                    lists.add(list);

                    found.add(point);
                    list.add(point);
                    int checkLocationNr = 1;
                    PixelPosition startPos = point;
                    int counter2 = 0;

                    while (true) {
                        PixelPosition checkPosition = neighborhood.get(checkLocationNr - 1).apply(point);

                        if (pixels.imageContainsPoint(checkPosition) && !pixels.isTransparent(checkPosition.x, checkPosition.y)) {
                            int newCheckLocationNr = checkLocationNrMap[checkLocationNr - 1];

                            if (checkPosition.equals(startPos)) {
                                    inside = true;
                                    break;
                            }

                            checkLocationNr = newCheckLocationNr;
                            point = checkPosition;
                            counter2 = 0;
                            found.add(point);
                            list.add(point);
                        } else {
                            checkLocationNr = 1 + (checkLocationNr % 8);
                            if (counter2 > 8) {
                                break;
                            } else {
                                counter2++;
                            }
                        }
                    }
                }
            }
        }

        for(List<PixelPosition> perimeter : lists){
            correctChamferedCorners(perimeter, pixels);
        }

        return lists;
    }

    /**
     * This corrects the 1 pixel chamfered corners that are created by the Moore neighborhood algorithm. It does this by
     * checking if a corner has been cut on an otherwise straight line, and if so, it inserts a new point into the perimeter.
     *
     * Theoretically the smoothing algorithm could take it back out again but it gives it a better starting shape so
     * it's likely not to.
     */
    private static void correctChamferedCorners(List<PixelPosition> perimeter, PixelProvider pixels) {
        // Iterate over the points in the perimeter list
        int stopLength =  perimeter.size();
        int originalSize = perimeter.size();
        for (int i = 0; i < stopLength; i++) {
            PixelPosition a = perimeter.get(i);
            PixelPosition b = perimeter.get((i + 1) % perimeter.size());
            PixelPosition c = perimeter.get((i + 2) % perimeter.size());
            PixelPosition d = perimeter.get((i + 3) % perimeter.size());


            boolean abHorizontal = a.y == b.y;
            boolean cdHorizontal = c.y == d.y;

            // Potential corner point (p)
            PixelPosition p;

            if (abHorizontal && !cdHorizontal) {
                p = new PixelPosition(c.x, a.y);
            } else if (!abHorizontal && cdHorizontal) {
                p = new PixelPosition(a.x, c.y);
            } else {
                continue;
            }

            if (p.equals(b) || p.equals(c)) {
                continue;
            }

            // Check if a-b and c-d form straight lines and meet at p
            if (isStraightLine(a, b, p) && isStraightLine(c, d, p)) {
                // Check if p is a non-transparent pixel
                if (!pixels.isTransparent(p.x, p.y)) {
                    // Insert p into the perimeter
                    perimeter.add((i + 2) % perimeter.size(), p);
                    i++; // Skip the next point as we just added a new point
                    stopLength++;
                }
            }
        }
    }

    private static boolean isStraightLine(PixelPosition a, PixelPosition b, PixelPosition p) {
        // Check if a, b, and p are in a straight line (either horizontally or vertically)
        return (a.x == b.x && b.x == p.x) || (a.y == b.y && b.y == p.y);
    }

}