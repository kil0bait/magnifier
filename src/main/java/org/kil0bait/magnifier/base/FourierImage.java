package org.kil0bait.magnifier.base;

public class FourierImage {
    private final ComplexNumber[][] pixels;
    private final int height;
    private final int width;

    public FourierImage(MagniImage image) {
        height = image.getHeight();
        width = image.getWidth();
        pixels = new ComplexNumber[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                pixels[y][x] = new ComplexNumber(image.pixel(y, x).getIntensity(), 0);
    }

    public FourierImage(ComplexNumber[][] pixels) {
        this.pixels = pixels;
        this.height = pixels.length;
        this.width = pixels[0].length;
    }

    public FourierImage dftSlowForward() {
        ComplexNumber[][] res = new ComplexNumber[height][width];
        int centerY = this.height / 2;
        int centerX = this.width / 2;
        for (int k1 = 0; k1 < height; k1++) {
            for (int k2 = 0; k2 < width; k2++) {
                ComplexNumber temp = ComplexNumber.zero();
                for (int n1 = 0; n1 < height; n1++)
                    for (int n2 = 0; n2 < width; n2++)
                        temp.sumHere(omegaForward(height, (centerY - k1) * (centerY - n1))
                                .mul(omegaForward(width, (k2 - centerX) * (n2 - centerX)))
                                .mul(pixels[n1][n2]));
                res[k1][k2] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

    public FourierImage dftSlowInverse() {
        ComplexNumber[][] res = new ComplexNumber[height][width];
        int centerY = this.height / 2;
        int centerX = this.width / 2;
        for (int k1 = 0; k1 < height; k1++) {
            for (int k2 = 0; k2 < width; k2++) {
                ComplexNumber temp = ComplexNumber.zero();
                for (int n1 = 0; n1 < height; n1++)
                    for (int n2 = 0; n2 < width; n2++)
                        temp.sumHere(omegaInverse(height, (centerY - k1) * (centerY - n1))
                                .mul(omegaInverse(width, (k2 - centerX) * (n2 - centerX)))
                                .mul(pixels[n1][n2]));
                res[k1][k2] = temp.divByNumberHere((double) height * width);
            }
        }
        return new FourierImage(res);
    }

    public FourierImage fftCooleyForward() {
        MagniException.validateResolutionIsPowerOfTwo(height, width);
        ComplexNumber[][] raw = fftCooleyForwardRecursion(height, 1, 0, 0);
        ComplexNumber[][] res = new ComplexNumber[height][width];
        int center = height / 2;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = raw[(y - center + height) % height][(x - center + width) % width];
        return new FourierImage(res);
    }

    public FourierImage fftCooleyInverse() {
        MagniException.validateResolutionIsPowerOfTwo(height, width);
        ComplexNumber[][] shifted = new ComplexNumber[height][width];
        int center = height / 2;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                shifted[y][x] = pixels[(y - center + height) % height][(x - center + width) % width];
        ComplexNumber[][] res = new FourierImage(shifted).fftCooleyInverseRecursion(height, 1, 0, 0);
        int sqrN = height * height;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < height; x++)
                res[y][x].divByNumberHere(sqrN);
        return new FourierImage(res);
    }

    public ComplexNumber[][] fftCooleyForwardRecursion(int n, int delta, int shift1, int shift2) {
        if (n == 1) {
            return new ComplexNumber[][]{{pixels[shift1][shift2]}};
        } else {
            int m = n / 2;
            ComplexNumber[][] s00 =
                    fftCooleyForwardRecursion(m, 2 * delta, shift1, shift2);
            ComplexNumber[][] s01 =
                    fftCooleyForwardRecursion(m, 2 * delta, shift1, delta + shift2);
            ComplexNumber[][] s10 =
                    fftCooleyForwardRecursion(m, 2 * delta, delta + shift1, shift2);
            ComplexNumber[][] s11 =
                    fftCooleyForwardRecursion(m, 2 * delta, delta + shift1, delta + shift2);
            ComplexNumber[][] res = new ComplexNumber[n][n];
            for (int k1 = 0; k1 < m; k1++)
                for (int k2 = 0; k2 < m; k2++) {
                    ComplexNumber s00MulOmega = s00[k1][k2];
                    ComplexNumber s01MulOmega = s01[k1][k2].mul(omegaForward(n, k2));
                    ComplexNumber s10MulOmega = s10[k1][k2].mul(omegaForward(n, k1));
                    ComplexNumber s11MulOmega = s11[k1][k2].mul(omegaForward(n, k1 + k2));
                    res[k1][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                    res[k1][k2 + m] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + m][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + m][k2 + m] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                }
            return res;
        }
    }

    public ComplexNumber[][] fftCooleyInverseRecursion(int n, int delta, int shift1, int shift2) {
        if (n == 1) {
            return new ComplexNumber[][]{{pixels[shift1][shift2]}};
        } else {
            int m = n / 2;
            ComplexNumber[][] s00 =
                    fftCooleyInverseRecursion(m, 2 * delta, shift1, shift2);
            ComplexNumber[][] s01 =
                    fftCooleyInverseRecursion(m, 2 * delta, shift1, delta + shift2);
            ComplexNumber[][] s10 =
                    fftCooleyInverseRecursion(m, 2 * delta, delta + shift1, shift2);
            ComplexNumber[][] s11 =
                    fftCooleyInverseRecursion(m, 2 * delta, delta + shift1, delta + shift2);
            ComplexNumber[][] res = new ComplexNumber[n][n];
            for (int k1 = 0; k1 < m; k1++)
                for (int k2 = 0; k2 < m; k2++) {
                    ComplexNumber s00MulOmega = s00[k1][k2];
                    ComplexNumber s01MulOmega = s01[k1][k2].mul(omegaInverse(n, k2));
                    ComplexNumber s10MulOmega = s10[k1][k2].mul(omegaInverse(n, k1));
                    ComplexNumber s11MulOmega = s11[k1][k2].mul(omegaInverse(n, k1 + k2));
                    res[k1][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                    res[k1][k2 + m] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + m][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + m][k2 + m] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                }
            return res;
        }
    }

    public MagniImage spectrumFourier() {
        MagniPixel[][] resPixels = new MagniPixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                resPixels[y][x] = new MagniPixel(pixels[y][x].length());
        return new MagniImage(resPixels);
    }

    public MagniImage imageFromRe() {
        MagniPixel[][] res = new MagniPixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = new MagniPixel(pixels[y][x].re);
        return new MagniImage(res);
    }

    public MagniImage imageFromIm() {
        MagniPixel[][] res = new MagniPixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = new MagniPixel(pixels[y][x].im);
        return new MagniImage(res);
    }

    private static ComplexNumber omegaForward(int n, int pow) {
        double x = -2 * Math.PI * pow / n;
        return new ComplexNumber(Math.cos(x), Math.sin(x));
    }

    private static ComplexNumber omegaInverse(int n, int pow) {
        double x = 2 * Math.PI * pow / n;
        return new ComplexNumber(Math.cos(x), Math.sin(x));
    }

    public String toStringRawValues() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("--Image resolution [%dx%d]--\r%n", height, width));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                builder.append(complexToString(pixels[y][x]));
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public static FourierImage fromStringRawValues(String s) {
        String[] split = s.replace("\r", "").split("\n");
        String[] temp = split[0].substring(split[0].indexOf("[") + 1, split[0].indexOf("]")).split("x");
        int height = Integer.parseInt(temp[0]);
        int width = Integer.parseInt(temp[1]);
        ComplexNumber[][] resPixels = new ComplexNumber[height][width];
        for (int y = 0; y < height; y++) {
            temp = split[y + 1].split(SPACES);
            for (int x = 0; x < width; x++)
                resPixels[y][x] = new ComplexNumber(ComplexNumber.parseComplex(temp[x]));
        }
        return new FourierImage(resPixels);
    }

    private static final int CELL_WIDTH = 64;
    private static final String SPACES = "\\s+";

    public static String complexToString(ComplexNumber g) {
        String res = g.toString();
        return res + (new String(new char[CELL_WIDTH - res.length()]).replace("\0", " "));
    }
}
