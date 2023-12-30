package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class MooreNeighbourhood {

    /**
     * Outputs the edges and holes of the given image. Note that the outer edges are wound anticlockwise, and the holes are wound clockwise.
     */
    static List<List<Point>> detectPerimeter(BufferedImage bufferedImage) {
        return detectPerimeter(new Pixels(bufferedImage));
    }

    /**
     * Outputs the edges and holes of the given image. Note that the outer edges are wound anticlockwise, and the holes are wound clockwise.
     */
    static List<List<Point>> detectPerimeter(Pixels pixels) {
        int width = pixels.size.width;
        int height = pixels.size.height;
        HashSet<Point> found = new HashSet<>();
        List<Point> list;
        List<List<Point>> lists = new ArrayList<>();
        boolean inside = false;

        List<Function<Point, Point>> neighborhood = List.of(
                point -> new Point(point.x - 1, point.y),
                point -> new Point(point.x - 1, point.y - 1),
                point -> new Point(point.x, point.y - 1),
                point -> new Point(point.x + 1, point.y - 1),
                point -> new Point(point.x + 1, point.y),
                point -> new Point(point.x + 1, point.y + 1),
                point -> new Point(point.x, point.y + 1),
                point -> new Point(point.x - 1, point.y + 1)
        );
        int[] checkLocationNrMap = new int[]{7, 7, 1, 1, 3, 3, 5, 5};

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Point point = new Point(x, y);
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
                    Point startPos = point;
                    int counter = 0;
                    int counter2 = 0;

                    while (true) {
                        Point checkPosition = neighborhood.get(checkLocationNr - 1).apply(point);

                        if (pixels.contains(checkPosition) && !pixels.isTransparent(checkPosition.x, checkPosition.y)) {
                            int newCheckLocationNr = checkLocationNrMap[checkLocationNr - 1];

                            if (checkPosition.equals(startPos)) {
                                counter++;
                                if (newCheckLocationNr == 1 || counter >= 3) {
                                    inside = true;
                                    break;
                                }
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

        for(List<Point> perimeter : lists){
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
    private static void correctChamferedCorners(List<Point> perimeter, Pixels pixels) {
        // Iterate over the points in the perimeter list
        int stopLength =  perimeter.size();
        int originalSize = perimeter.size();
        for (int i = 0; i < stopLength; i++) {
            Point a = perimeter.get(i);
            Point b = perimeter.get((i + 1) % perimeter.size());
            Point c = perimeter.get((i + 2) % perimeter.size());
            Point d = perimeter.get((i + 3) % perimeter.size());


            boolean abHorizontal = a.y == b.y;
            boolean cdHorizontal = c.y == d.y;

            // Potential corner point (p)
            Point p;

            if (abHorizontal && !cdHorizontal) {
                p = new Point(c.x, a.y);
            } else if (!abHorizontal && cdHorizontal) {
                p = new Point(a.x, c.y);
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

    private static boolean isStraightLine(Point a, Point b, Point p) {
        // Check if a, b, and p are in a straight line (either horizontally or vertically)
        return (a.x == b.x && b.x == p.x) || (a.y == b.y && b.y == p.y);
    }

    static class Pixels {
        final java.awt.Dimension size;
        BufferedImage image;
        Pixels(BufferedImage image) {
            size = new java.awt.Dimension(image.getWidth(), image.getHeight());
            this.image = image;
        }

        boolean isTransparent(int x, int y) {
            if (!contains(new Point(x, y))) {
                return true;
            }
            int pixel = image.getRGB(x, y);
            int alpha = (pixel >> 24) & 0xff;
            return alpha == 0;
        }

        boolean contains(Point point) {
            return point.x >= 0 && point.x < size.width && point.y >= 0 && point.y < size.height;
        }

    }

}