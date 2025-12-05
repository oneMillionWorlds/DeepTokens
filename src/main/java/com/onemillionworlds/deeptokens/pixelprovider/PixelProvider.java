package com.onemillionworlds.deeptokens.pixelprovider;

/**
 * An interface to allow the consumer to provide pixels in whatever way they want.
 * BufferedImage remains an option but on android something else can be wrapped instead.
 */
public interface PixelProvider {

    boolean isTransparent(int x, int y);

    int getImageWidth();
    int getImageHeight();

    default boolean imageContainsPoint(PixelPosition p){
        return imageContainsPoint(p.getX(), p.getY());
    }

    default boolean imageContainsPoint(int x, int y){
        return x >= 0 && x < getImageWidth() && y >= 0 && y < getImageHeight();
    }
}
