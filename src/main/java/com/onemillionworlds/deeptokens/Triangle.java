package com.onemillionworlds.deeptokens;

import java.awt.Point;

public record Triangle(Point a, Point b, Point c){

    private boolean isPointInsideTriangle(Point p) {
        int ab = (a.x - p.x) * (b.y - a.y) - (a.y - p.y) * (b.x - a.x);
        int bc = (b.x - p.x) * (c.y - b.y) - (b.y - p.y) * (c.x - b.x);
        int ca = (c.x - p.x) * (a.y - c.y) - (c.y - p.y) * (a.x - c.x);
        return (ab >= 0 && bc >= 0 && ca >= 0) || (ab <= 0 && bc <= 0 && ca <= 0);
    }

}
