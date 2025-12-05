package com.onemillionworlds.deeptokens;

import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DouglasPeuckerLineSimplifierTest{

    @Test
    void simplify(){

        List<PixelPosition> points = List.of(
                new PixelPosition(0,0),
                new PixelPosition(1,0),
                new PixelPosition(3,0),
                new PixelPosition(3,0),
                new PixelPosition(3,1),
                new PixelPosition(3,2),
                new PixelPosition(3,3)
        );

        List<PixelPosition> simplified = DouglasPeuckerLineSimplifier.simplify(points, 0.5);
        List<PixelPosition> expected = List.of(
                new PixelPosition(0,0),
                new PixelPosition(3,0),
                new PixelPosition(3,3)
        );

        assertEquals(expected,simplified);
    }
}