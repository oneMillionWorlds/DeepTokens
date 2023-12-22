package com.onemillionworlds.deeptokens;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MooreNeighbourhood {

    static List<Point> detectPerimeter(BufferedImage bufferedImage) {
        return detectPerimeter(new Pixels(bufferedImage));
    }

    static List<Point> detectPerimeter(Pixels pixels) {
        int width = pixels.size.width;
        int height = pixels.size.height;
        List<Point> list;

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
                boolean isTransparent = pixels.isTransparent(x, y);
                if (!isTransparent) {
                    list = new ArrayList<>();
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
                                    return list;
                                }
                            }

                            checkLocationNr = newCheckLocationNr;
                            point = checkPosition;
                            counter2 = 0;
                            list.add(point);
                        } else {
                            checkLocationNr = 1 + (checkLocationNr % 8);
                            if (counter2 > 8) {
                                return list;
                            } else {
                                counter2++;
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("No perimeter found");
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