package com.onemillionworlds.deeptokens;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

/**
 * This gives the image an expanded "fuzzy edge". This is useful in case the simplification process leads to the shape
 * being slightly larger and going off the edge of the original image.
 */
public class ImageEdgeExpander{

    public static BufferedImage processImage(BufferedImage originalImage, int averagingDistance) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = originalImage.getRGB(0, 0, width, height, null, 0, width);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = pixels[x + y * width];
                int alpha = (pixel >> 24) & 0xff;

                if (alpha == 255) { // Not transparent
                    newImage.setRGB(x, y, pixel);
                } else { // Transparent
                    Color averageColor = getAverageColorAround(pixels, x, y, width, height, averagingDistance);
                    if (averageColor != null) {
                        newImage.setRGB(x, y, averageColor.getRGB());
                    }
                }
            }
        }

        /*
        File outputfile = new File("C:\\Users\\richa\\Downloads\\DeepTokenImages\\image.jpg");
        try{
            ImageIO.write(newImage, "png", outputfile);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        */


        return newImage;
    }

    private static Color getAverageColorAround(int[] pixels, int x, int y, int width, int height, int distance) {
        int redTotal = 0, greenTotal = 0, blueTotal = 0, count = 0;

        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
                    int pixel = pixels[x + y*width];
                    int alpha = (pixel >> 24) & 0xff;

                    if (alpha != 0) { // Not transparent
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = (pixel) & 0xff;

                        redTotal += red;
                        greenTotal += green;
                        blueTotal += blue;
                        count++;
                    }
                }
            }
        }

        return count > 0 ? new Color(redTotal / count, greenTotal / count, blueTotal / count) : null;
    }
}
