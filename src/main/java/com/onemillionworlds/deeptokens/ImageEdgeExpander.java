package com.onemillionworlds.deeptokens;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * This gives the image an expanded "fuzzy edge". This is useful in case the simplification process leads to the shape
 * being slightly larger and going off the edge of the original image.
 */
public class ImageEdgeExpander{

    public static BufferedImage processImage(BufferedImage originalImage, int averagingDistance) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Color[] pixels = new Color[width * height];

        for (int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int pixel = originalImage.getRGB(x, y);
                pixels[x + y * width] = new Color(pixel, true);
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixel = pixels[x + y * width];
                int alpha = pixel.getAlpha();

                if (alpha == 255) { // Not transparent
                    newImage.setRGB(x, y, pixels[x + y * width].getRGB());
                } else { // Transparent
                    Color averageColor = getAverageColorAround(pixels, x, y, width, height, averagingDistance);
                    if (averageColor != null) {
                        newImage.setRGB(x, y, averageColor.getRGB());
                    } else {
                        newImage.setRGB(x, y, pixel.getRGB()); // Keep original transparent pixel (Shouldn't ever actually be used)
                    }
                }
            }
        }

        return newImage;
    }

    private static Color getAverageColorAround(Color[] pixels, int x, int y, int width, int height, int distance) {
        int redTotal = 0, greenTotal = 0, blueTotal = 0, count = 0;

        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                    Color pixel = pixels[x + y*width];
                    int alpha = pixel.getAlpha();

                    if (alpha == 255) { // Not transparent
                        redTotal += pixel.getRed();
                        greenTotal += pixel.getGreen();
                        blueTotal += pixel.getBlue();
                        count++;
                    }
                }
            }
        }

        return count > 0 ? new Color(redTotal / count, greenTotal / count, blueTotal / count) : null;
    }
}
