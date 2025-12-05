package com.onemillionworlds.deeptokens.pixelprovider;

import java.awt.image.BufferedImage;

public class BufferedImagePixelProvider implements PixelProvider{

    int width;
    int height;
    BufferedImage image;

    public BufferedImagePixelProvider(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    @Override
    public boolean isTransparent(int x, int y) {
        if (!imageContainsPoint(x, y)) {
            return true;
        }

        // Treat pixels with zero alpha as transparent
        int argb = image.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        return alpha == 0;
    }

    @Override
    public int getImageWidth() {
        return width;
    }

    @Override
    public int getImageHeight() {
        return height;
    }
}
