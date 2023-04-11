package ru.kil0bait.magnifier.base;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.util.Locale;

public class MagniImage {
    private final MagniPixel[][] pixels;
    private final int height;
    private final int width;
    private String name;

    public MagniImage(MagniPixel[][] pixels) {
        this.height = pixels.length;
        this.width = pixels[0].length;
        this.pixels = pixels;
    }

    public MagniImage(BufferedImage bufferedImage) {
        height = bufferedImage.getHeight();
        width = bufferedImage.getWidth();
        pixels = new MagniPixel[height][width];
        if (bufferedImage.getColorModel().getColorSpace().equals(ColorSpace.getInstance(ColorSpace.CS_GRAY)))
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    pixels[y][x] = MagniPixel.fromGrayColor(bufferedImage.getRGB(x, y));
        else
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    pixels[y][x] = MagniPixel.fromRGBColor(bufferedImage.getRGB(x, y));
    }

    public MagniImage(MagniImage that) {
        this.height = that.height;
        this.width = that.width;
        this.name = that.name;
        this.pixels = new MagniPixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                this.pixels[y][x] = new MagniPixel(that.pixels[y][x]);
    }

    public MagniImage subtract(MagniImage that) {
        checkImagesResolutions(this, that);
        MagniImage res = new MagniImage(this);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res.pixels[y][x] = this.pixels[y][x].subtractIntensity(that.pixels[y][x]);
        return res;
    }

    public MagniPixel pixel(int y, int x) {
        return pixels[y][x];
    }

    public MagniImage negative() {
        MagniPixel[][] res = new MagniPixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = new MagniPixel(-pixels[y][x].getIntensity());
        return new MagniImage(res);
    }

    public MagniImage dynamicNorm() {
        MagniPixel[][] res = new MagniPixel[height][width];
        double min = 0, max = 0;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                min = Math.min(pixels[y][x].getIntensity(), min);
                max = Math.max(pixels[y][x].getIntensity(), max);
            }
        System.out.println("Min = " + min + "; Max = " + max);
        max -= min;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                double t = pixels[y][x].getIntensity();
                t = ((t - min) / max) * 255;
                res[y][x] = new MagniPixel(t);
            }
        return new MagniImage(res);
    }

    public MagniImage dynamicNormAverage() {
        MagniPixel[][] res = new MagniPixel[height][width];
        double min = 0, max = 0;
        double average = 0;
        double temp;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                temp = pixels[y][x].getIntensity();
                min = Math.min(temp, min);
                max = Math.max(temp, max);
                average += temp;
            }
        System.out.println(min + " " + max);
        average /= (width * height);
        max = 2 * (average - min);
        double threshold = max - min;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if ((temp = pixels[y][x].getIntensity()) > threshold) {
                    res[y][x] = new MagniPixel(255);
                    continue;
                }
                temp = ((temp - min) / max) * 255;
                res[y][x] = new MagniPixel(temp);
            }
        return new MagniImage(res);
    }

    public BufferedImage toBufferedImage() {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res.setRGB(x, y, pixels[y][x].getRGBInt());
        return res;
    }

    public BufferedImage toGrayImage() {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res.setRGB(x, y, pixels[y][x].getRGBInt());
        return res;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void checkImagesResolutions(MagniImage image1, MagniImage image2) {
        if (image1.width != image2.width || image1.height != image2.height)
            throw new MagniException("Images resolutions are not equal");
    }

    private static final int CELL_WIDTH = 24;
    private static final String SPACES = "\\s+";
    private static final String NUMBER_FORMAT = "%.6f";

    public String toStringRawValues() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("--Image resolution [%dx%d]--\r\n", height, width));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                builder.append(numberToString(pixels[y][x].getIntensity()));
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public static MagniImage fromStringRawValues(String s) {
        String[] split = s.replaceAll("\r", "").split("\n");
        String[] temp = split[0].substring(split[0].indexOf("[") + 1, split[0].indexOf("]")).split("x");
        int height = Integer.parseInt(temp[0]);
        int width = Integer.parseInt(temp[1]);
        MagniPixel[][] resPixels = new MagniPixel[height][width];
        for (int y = 0; y < height; y++) {
            temp = split[y + 1].split(SPACES);
            for (int x = 0; x < width; x++)
                resPixels[y][x] = new MagniPixel(Double.parseDouble(temp[x]));
        }
        return new MagniImage(resPixels);
    }

    public static String numberToString(double n) {
        String res = String.format(Locale.US, NUMBER_FORMAT, n);
        return res + (" ".repeat(CELL_WIDTH - res.length()));
    }
}
