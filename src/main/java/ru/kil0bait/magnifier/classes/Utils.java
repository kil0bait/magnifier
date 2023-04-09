package ru.kil0bait.magnifier.classes;

import java.util.Random;

public class Utils {
    private static final Random random = new Random();

    public static MagniImage generateImage(int resolution) {
        MagniPixel[][] res = new MagniPixel[resolution][resolution];
        for (int i = 0; i < resolution; i++)
            for (int j = 0; j < resolution; j++)
                res[i][j] = new MagniPixel(randomNumber());
        return new MagniImage(res);
    }

    public static MagniImage generateImageFrom(MagniImage image, double percent) {
        int resolution = image.getWidth();
        MagniPixel[][] res = new MagniPixel[resolution][resolution];
        for (int y = 0; y < resolution; y++)
            for (int x = 0; x < resolution; x++)
                res[y][x] = new MagniPixel(randomNumber(image.pixel(y, x).getIntensity(), percent));
        return new MagniImage(res);
    }

    public static MagniImage generateAbsoluteImageFrom(MagniImage image, double absolutePercent, double absoluteValue) {
        int resolution = image.getWidth();
        MagniPixel[][] res = new MagniPixel[resolution][resolution];
        for (int y = 0; y < resolution; y++)
            for (int x = 0; x < resolution; x++)
//                res[y][x] = new MagniPixel(randomNumberWithAbsoluteRange(image.pixel(x, y).getIntensity(),
//                        absolutePercent, absoluteValue));
                res[y][x] = new MagniPixel(image.pixel(y, x).getIntensity() - random.nextDouble(absolutePercent * absoluteValue));
        return new MagniImage(res);
    }

    public static double randomNumber() {
        return random.nextDouble();
    }

    public static double randomNumber(double source, double percent) {
        return random.nextDouble(source * (1 - percent), source * (1 + percent) + 0.1F);
    }

    public static double randomNumberWithAbsoluteRange(double source, double absolutePercent, double absoluteValue) {
        return random.nextDouble(source - absolutePercent * absoluteValue,
                source + absolutePercent * absoluteValue);
    }
}
