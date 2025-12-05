package com.onemillionworlds.deeptokens.pixelprovider;

public class FlippedPixelProvider implements PixelProvider {
    PixelProvider underlying;
    public FlippedPixelProvider(PixelProvider underlying) {
        this.underlying = underlying;
    }

    @Override
    public boolean isTransparent(int x, int y) {
        return underlying.isTransparent(x, getImageHeight() - y - 1);
    }

    @Override
    public int getImageWidth() {
        return underlying.getImageWidth();
    }

    @Override
    public int getImageHeight() {
        return underlying.getImageHeight();
    }
}
