package com.onemillionworlds.deeptokens;

import java.awt.Point;

public class Triangle{
    private final Point a;
    private final Point b;
    private final Point c;

    public Triangle(Point a, Point b, Point c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Point a(){
        return a;
    }

    public Point b(){
        return b;
    }

    public Point c(){
        return c;
    }
}
