package ru.kil0bait.magnifier.base;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FourierImage {

    private final int width;
    private final ComplexNumber[][] pixels;

    public FourierImage(MagniImage image) {
        if (image.getWidth() != image.getHeight())
            throw new MagniException("Bad resolution of image : width not equals height");
        width = image.getWidth();
        pixels = new ComplexNumber[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                pixels[y][x] = new ComplexNumber(image.pixel(y, x).getIntensity(), 0);
    }

    public FourierImage(ComplexNumber[][] pixels) {
        width = pixels.length;
        for (int y = 0; y < pixels.length; y++)
            if (pixels[y].length != width)
                throw new MagniException("Bad resolution of image : width not equals height");
        this.pixels = pixels;
    }

    public FourierImage dftSlowForward() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber[] omegas = omegasArrayForward(N);
        for (int k1 = 0; k1 < N; k1++) {
            for (int k2 = 0; k2 < N; k2++) {
                ComplexNumber temp = ComplexNumber.zero();
                for (int n1 = 0; n1 < N; n1++)
                    for (int n2 = 0; n2 < N; n2++)
                        temp.sumHere(omegas[k1 * n1 + k2 * n2].mul(pixels[n1][n2]));
                res[k1][k2] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

    public FourierImage dftSlowInverse() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber[] omegas = omegasArrayInverse(N);
        for (int k1 = 0; k1 < N; k1++) {
            for (int k2 = 0; k2 < N; k2++) {
                ComplexNumber temp = ComplexNumber.zero();
                for (int n1 = 0; n1 < N; n1++)
                    for (int n2 = 0; n2 < N; n2++) {
                        temp.sumHere(omegas[k1 * n1 + k2 * n2].mul(pixels[n1][n2]));
                    }
                temp.divByNumberHere(N * N);
                res[k1][k2] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

    public FourierImage dftSlowInverseWithCenter() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        int center = width / 2;
        for (int k1 = 0; k1 < N; k1++) {
            System.out.println(k1);
            for (int k2 = 0; k2 < N; k2++) {
                ComplexNumber temp = ComplexNumber.zero();
                for (int n1 = 0; n1 < N; n1++)
                    for (int n2 = 0; n2 < N; n2++)
                        temp.sumHere(omegaInverseApril(N, (center - k1) * (center - n1) + (k2 - center) * (n2 - center)).mul(pixels[n1][n2]));
                temp.divByNumberHere(N * N);
                res[k1][k2] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

    public FourierImage fftCooleyForward() {
        if (!isPowerOfTwo(width))
            throw new MagniException("Bad resolution of image for Cooley FFT");
        return new FourierImage(fftCooleyForwardRecursion(width, 1, 0, 0));
    }

    public FourierImage fftCooleyInverse() {
        if (!isPowerOfTwo(width))
            throw new MagniException("Bad resolution of image for Cooley FFT");
        ComplexNumber[][] res = fftCooleyInverseRecursion(width, 1, 0, 0);
        int sqrN = width * width;
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x].divByNumberHere(sqrN);
        return new FourierImage(res);
    }

    public FourierImage shiftApril() {
        ComplexNumber[][] res = new ComplexNumber[width][width];
        int center = width / 2;
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                res[y][x] = pixels[(y - center + width) % width][(x - center + width) % width];
            }
        }
        return new FourierImage(res);
    }

    public ComplexNumber[][] fftCooleyForwardRecursion(int N, int delta, int shift1, int shift2) {
        if (N == 1) {
            return new ComplexNumber[][]{{pixels[shift1][shift2]}};
        } else {
            int M = N / 2;
            ComplexNumber[][] s00 =
                    fftCooleyForwardRecursion(M, 2 * delta, shift1, shift2);
            ComplexNumber[][] s01 =
                    fftCooleyForwardRecursion(M, 2 * delta, shift1, delta + shift2);
            ComplexNumber[][] s10 =
                    fftCooleyForwardRecursion(M, 2 * delta, delta + shift1, shift2);
            ComplexNumber[][] s11 =
                    fftCooleyForwardRecursion(M, 2 * delta, delta + shift1, delta + shift2);
            ComplexNumber[][] res = new ComplexNumber[N][N];
            for (int k1 = 0; k1 < M; k1++)
                for (int k2 = 0; k2 < M; k2++) {
                    ComplexNumber s00MulOmega = s00[k1][k2];
                    ComplexNumber s01MulOmega = s01[k1][k2].mul(omegaForward(N, k2));
                    ComplexNumber s10MulOmega = s10[k1][k2].mul(omegaForward(N, k1));
                    ComplexNumber s11MulOmega = s11[k1][k2].mul(omegaForward(N, k1 + k2));
                    res[k1][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                    res[k1][k2 + M] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2 + M] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                }
            return res;
        }
    }

    public ComplexNumber[][] fftCooleyInverseRecursion(int N, int delta, int shift1, int shift2) {
        if (N == 1) {
            return new ComplexNumber[][]{{pixels[shift1][shift2]}};
        } else {
            int M = N / 2;
            ComplexNumber[][] s00 =
                    fftCooleyInverseRecursion(M, 2 * delta, shift1, shift2);
            ComplexNumber[][] s01 =
                    fftCooleyInverseRecursion(M, 2 * delta, shift1, delta + shift2);
            ComplexNumber[][] s10 =
                    fftCooleyInverseRecursion(M, 2 * delta, delta + shift1, shift2);
            ComplexNumber[][] s11 =
                    fftCooleyInverseRecursion(M, 2 * delta, delta + shift1, delta + shift2);
            ComplexNumber[][] res = new ComplexNumber[N][N];
            for (int k1 = 0; k1 < M; k1++)
                for (int k2 = 0; k2 < M; k2++) {
                    ComplexNumber s00MulOmega = s00[k1][k2];
                    ComplexNumber s01MulOmega = s01[k1][k2].mul(omegaInverse(N, k2));
                    ComplexNumber s10MulOmega = s10[k1][k2].mul(omegaInverse(N, k1));
                    ComplexNumber s11MulOmega = s11[k1][k2].mul(omegaInverse(N, k1 + k2));
                    res[k1][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                    res[k1][k2 + M] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2 + M] = ComplexNumber.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                }
            return res;
        }
    }

    public MagniImage magnitude() {
        MagniPixel[][] resPixels = new MagniPixel[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                resPixels[y][x] = new MagniPixel(pixels[y][x].length());
        MagniImage res = new MagniImage(resPixels);
        return res.dynamicNormAverage();
    }

    public MagniImage getImageFromRe() {
        MagniPixel[][] res = new MagniPixel[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = new MagniPixel(pixels[y][x].getRe());
        return new MagniImage(res);
    }

    public MagniImage getImageFromIm() {
        MagniPixel[][] res = new MagniPixel[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = new MagniPixel(pixels[y][x].getIm());
        return new MagniImage(res);
    }

    public MagniImage getImageFromLength() {
        MagniPixel[][] res = new MagniPixel[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = new MagniPixel(pixels[y][x].length());
        return new MagniImage(res);
    }

    public ComplexNumber[][] getPixels() {
        return pixels;
    }

    public static ComplexNumber[] omegasArrayForward(int N) {
        ComplexNumber[] res = new ComplexNumber[2 * N * N];
        for (int i = 0; i < res.length; i++)
            res[i] = omegaForward(N, i);
        return res;
    }

    public static ComplexNumber[] omegasArrayInverse(int N) {
        ComplexNumber[] res = new ComplexNumber[2 * N * N];
        for (int i = 0; i < res.length; i++)
            res[i] = omegaInverse(N, i);
        return res;
    }

    public static ComplexNumber omegaForward(int N, int pow) {
        double re, im;
        re = Math.cos(2 * pow * Math.PI / N);
        im = Math.sin(-2 * pow * Math.PI / N);
        return new ComplexNumber(re, im);
    }

    public static ComplexNumber omegaInverse(int N, int pow) {
        double re, im;
        re = Math.cos(2 * pow * Math.PI / N);
        im = Math.sin(2 * pow * Math.PI / N);
        return new ComplexNumber(re, im);
    }

    public static ComplexNumber omegaInverseApril(int N, int pow) {
        double x = 2 * Math.PI * pow / N;
        return new ComplexNumber(Math.cos(x), Math.sin(x));
    }

    public static boolean isPowerOfTwo(int N) {
        return N != 0 && ((N & (N - 1)) == 0);
    }

    public String rawValues() {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++)
                builder.append(complexToString(pixels[y][x]));
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public void saveToFile(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(String.format("--Image resolution [%dx%d]--\r\n", width, width));
        writer.write("--Real part--\r\n");
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++)
                writer.write(complexToString(pixels[y][x]));
            writer.newLine();
        }
        writer.close();
    }

    private static final String NUMBER_FORMAT = "%.6f";
    private static final String SPACES = "\\s+";
    private static final int CELL_WIDTH = 64;

    public static String complexToString(ComplexNumber g) {
        String res = g.toString();
        return res + (" ".repeat(CELL_WIDTH - res.length()));
    }
}
