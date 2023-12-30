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
                        int newCheckLocationNr = checkLocationNrMap[checkLocationNr - 1];

                        if (pixels.contains(checkPosition) && !pixels.isTransparent(checkPosition.x, checkPosition.y)) {
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
                                counter2 = 0;
                                break;
                            } else {
                                counter2++;
                            }
                        }
                    }
                }
            }
        }
        return lists;
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