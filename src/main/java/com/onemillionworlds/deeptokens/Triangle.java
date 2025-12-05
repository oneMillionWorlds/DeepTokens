package com.onemillionworlds.deeptokens;

import com.onemillionworlds.deeptokens.pixelprovider.PixelPosition;

import java.awt.Point;

public class Triangle{
    private final PixelPosition a;
    private final PixelPosition b;
    private final PixelPosition c;

    public Triangle(PixelPosition a, PixelPosition b, PixelPosition c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public PixelPosition a(){
        return a;
    }

    public PixelPosition b(){
        return b;
    }

    public PixelPosition c(){
        return c;
    }
}
