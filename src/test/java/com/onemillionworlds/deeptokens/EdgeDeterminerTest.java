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