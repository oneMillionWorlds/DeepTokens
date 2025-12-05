package com.onemillionworlds.deeptokens.pixelprovider;

import java.util.Objects;

public class PixelPosition {
    public final int x;
    public final int y;

    public PixelPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PixelPosition)) return false;
        PixelPosition that = (PixelPosition) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public double distance(PixelPosition other) {
        return Math.hypot(other.x - x, other.y - y);
    }
}
