package com.onemillionworlds.deeptokens;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DouglasPeuckerLineSimplifierTest{

    @Test
    void simplify(){

        List<Point> points = List.of(
                new Point(0,0),
                new Point(1,0),
                new Point(3,0),
                new Point(3,0),
                new Point(3,1),
                new Point(3,2),
                new Point(3,3)
        );

        List<Point> simplified = DouglasPeuckerLineSimplifier.simplify(points, 0.5);
        List<Point> expected = List.of(
                new Point(0,0),
                new Point(3,0),
                new Point(3,3)
        );

        assertEquals(expected,simplified);
    }
}