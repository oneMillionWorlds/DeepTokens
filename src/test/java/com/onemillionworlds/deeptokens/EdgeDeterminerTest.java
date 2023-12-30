package com.onemillionworlds.deeptokens;

import org.junit.jupiter.api.Test;
import z.asserts.CollectionAsserts;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeDeterminerTest{

    @Test
    void determinePerimeter_simpleSquare(){
        BufferedImage bufferedImage = transparentImage(5, 5);

        for(int x = 1; x<4; x++){
            for(int y = 1; y<4; y++){
                bufferedImage.setRGB(x, y, Color.RED.getRGB());
            }
        }

        List<List<Point>> edgePoints = MooreNeighbourhood.detectPerimeter(bufferedImage);
        List<Point> expectedEdgePoints = List.of(
                new Point(1, 1),
                new Point(2, 1),
                new Point(3, 1),
                new Point(3, 2),
                new Point(3, 3),
                new Point(2, 3),
                new Point(1, 3),
                new Point(1, 2)
            );

        assertEquals(1, edgePoints.size());
        CollectionAsserts.assertEqualContents(expectedEdgePoints, edgePoints.get(0));
    }

    @Test
    void determinePerimeter_simpleHole(){
        BufferedImage bufferedImage = transparentImage(8, 8);

        for(int x = 1; x<=6; x++){
            for(int y = 1; y<=6; y++){
                bufferedImage.setRGB(x, y, Color.RED.getRGB());
            }
        }

        for(int x = 3; x<=4; x++){
            for(int y = 3; y<=4; y++){
                bufferedImage.setRGB(x, y, new Color(0,0,0,0).getRGB());
            }
        }


        List<List<Point>> edgePoints = MooreNeighbourhood.detectPerimeter(bufferedImage);
        List<Point> expectedOuterEdgePoints = List.of(
                new Point(1, 1),
                new Point(2, 1),
                new Point(3, 1),
                new Point(4, 1),
                new Point(5, 1),
                new Point(6, 1),
                new Point(6, 2),
                new Point(6, 3),
                new Point(6, 4),
                new Point(6, 5),
                new Point(6, 6),
                new Point(5, 6),
                new Point(4, 6),
                new Point(3, 6),
                new Point(2, 6),
                new Point(1, 6),
                new Point(1, 5),
                new Point(1, 4),
                new Point(1, 3),
                new Point(1, 2)
                );

        List<Point> expectedInnerEdgePoints = List.of(
                new Point(2, 2),
                new Point(2, 3),
                new Point(2, 4),
                new Point(2, 5),
                new Point(3, 5),
                new Point(4, 5),
                new Point(5, 5),
                new Point(5, 4),
                new Point(5, 3),
                new Point(5, 2),
                new Point(4, 2),
                new Point(3, 2)

        );

        assertEquals(2, edgePoints.size());
        CollectionAsserts.assertEqualContents(expectedOuterEdgePoints, edgePoints.get(0));
        CollectionAsserts.assertEqualContents(expectedInnerEdgePoints, edgePoints.get(1));
    }

    @Test
    void determinePerimeter_diamondHole(){
        BufferedImage bufferedImage = transparentImage(9, 9);

        for(int x = 1; x<=7; x++){
            for(int y = 1; y<=7; y++){
                bufferedImage.setRGB(x, y, Color.RED.getRGB());
            }
        }

        bufferedImage.setRGB(3, 4, new Color(0,0,0,0).getRGB());
        bufferedImage.setRGB(4, 3, new Color(0,0,0,0).getRGB());
        bufferedImage.setRGB(4, 4, new Color(0,0,0,0).getRGB());
        bufferedImage.setRGB(5, 4, new Color(0,0,0,0).getRGB());
        bufferedImage.setRGB(4, 5, new Color(0,0,0,0).getRGB());

        List<List<Point>> edgePoints = MooreNeighbourhood.detectPerimeter(bufferedImage);

        //not testing outer edge because other tests do that

        List<Point> expectedInnerEdgePoints = List.of(
                new Point(2, 4),
                new Point(3, 3),
                new Point(4, 2),
                new Point(5, 3),
                new Point(6, 4),
                new Point(5, 5),
                new Point(4, 6),
                new Point(3, 5)

        );

        assertEquals(2, edgePoints.size());
        CollectionAsserts.assertEqualContents(expectedInnerEdgePoints, edgePoints.get(1));
    }

    public BufferedImage transparentImage(int width, int height){
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                bufferedImage.setRGB(x, y, 0x00000000);
            }
        }
        return bufferedImage;
    }
}