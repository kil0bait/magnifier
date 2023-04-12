package org.kil0bait.magnifier.base;

public class MagniException extends RuntimeException {
    public MagniException(String message) {
        super(message);
    }

    public static void validateHeightEqualsWidth(int height, int width) {
        if (height != width)
            throw new MagniException("Bad resolution of image: width and height are not equal");
    }

    public static void validateResolutionIsPowerOfTwo(int height, int width) {
        validateHeightEqualsWidth(height, width);
        if (!isPowerOfTwo(height))
            throw new MagniException("Bad resolution of image: width is not power of 2");
    }

    public static void validateEqualResolutions(MagniImage image1, MagniImage image2) {
        if (image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth())
            throw new MagniException("Bad resolution: images have different resolution");
    }

    private static boolean isPowerOfTwo(int N) {
        return N != 0 && ((N & (N - 1)) == 0);
    }
}
