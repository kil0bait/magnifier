package ru.kil0bait.magnifier.vector;

import ru.kil0bait.magnifier.base.*;

public class ComplexVectorImage {
    private final ComplexVector[][] pixels;
    private final int width;

    public ComplexVectorImage(MagniImage image1, MagniImage image2) {
        if (!(isSquare(image1) && isSquare(image2) && image1.getWidth() == image2.getWidth()))
            throw new MagniException("Images are not square or have different resolution");
        width = image1.getWidth();
        pixels = new ComplexVector[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                pixels[y][x] = new ComplexVector(image1.pixel(y, x).getIntensity(), image2.pixel(y, x).getIntensity());
    }

    private ComplexVectorImage(ComplexVector[][] pixels) {
        this.pixels = pixels;
        this.width = pixels.length;
    }

    private static boolean isSquare(MagniImage image) {
        return image.getWidth() == image.getHeight();
    }


    public ComplexVectorImage fftCooleyForwardWithShift() {
        ComplexVector[][] raw = fftCooleyForwardRecursion(width, 1, 0, 0);
        ComplexVector[][] res = new ComplexVector[width][width];
        int center = width / 2;
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                res[y][x] = raw[(y - center + width) % width][(x - center + width) % width];
            }
        }
        return new ComplexVectorImage(res);
    }

    public ComplexVectorImage fftCooleyForward() {
        return new ComplexVectorImage(fftCooleyForwardRecursion(width, 1, 0, 0));
    }

    public ComplexVector[][] fftCooleyForwardRecursion(int N, int delta, int shift1, int shift2) {
        if (N == 1) {
            return new ComplexVector[][]{{pixels[shift1][shift2]}};
        } else {
            int M = N / 2;
            ComplexVector[][] s00 =
                    fftCooleyForwardRecursion(M, 2 * delta, shift1, shift2);
            ComplexVector[][] s01 =
                    fftCooleyForwardRecursion(M, 2 * delta, shift1, delta + shift2);
            ComplexVector[][] s10 =
                    fftCooleyForwardRecursion(M, 2 * delta, delta + shift1, shift2);
            ComplexVector[][] s11 =
                    fftCooleyForwardRecursion(M, 2 * delta, delta + shift1, delta + shift2);
            ComplexVector[][] res = new ComplexVector[N][N];
            for (int k1 = 0; k1 < M; k1++)
                for (int k2 = 0; k2 < M; k2++) {
                    ComplexVector s00MulOmega = s00[k1][k2];
                    ComplexVector s01MulOmega = s01[k1][k2].mul(omegaForward(N, k2));
                    ComplexVector s10MulOmega = s10[k1][k2].mul(omegaForward(N, k1));
                    ComplexVector s11MulOmega = s11[k1][k2].mul(omegaForward(N, k1 + k2));
                    res[k1][k2] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                    res[k1][k2 + M] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2 + M] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                }
            return res;
        }
    }

    public ComplexVectorImage dftSlowForward() {
        int N = width;
        ComplexVector[][] res = new ComplexVector[N][N];
        int center = width / 2;
        for (int k1 = 0; k1 < N; k1++) {
            System.out.println(k1);
            for (int k2 = 0; k2 < N; k2++) {
                ComplexVector temp = ComplexVector.zero();
                for (int n1 = 0; n1 < N; n1++)
                    for (int n2 = 0; n2 < N; n2++)
                        temp.sumHere(pixels[n1][n2].mul(omegaForwardApril(N, (center - k1) * (center - n1) + (k2 - center) * (n2 - center))));
                res[k1][k2] = temp;
            }
        }
        return new ComplexVectorImage(res);
    }

    public ComplexVector[][] fftCooleyInverseRecursion(int N, int delta, int shift1, int shift2) {
        if (N == 1) {
            return new ComplexVector[][]{{pixels[shift1][shift2]}};
        } else {
            int M = N / 2;
            ComplexVector[][] s00 =
                    fftCooleyInverseRecursion(M, 2 * delta, shift1, shift2);
            ComplexVector[][] s01 =
                    fftCooleyInverseRecursion(M, 2 * delta, shift1, delta + shift2);
            ComplexVector[][] s10 =
                    fftCooleyInverseRecursion(M, 2 * delta, delta + shift1, shift2);
            ComplexVector[][] s11 =
                    fftCooleyInverseRecursion(M, 2 * delta, delta + shift1, delta + shift2);
            ComplexVector[][] res = new ComplexVector[N][N];
            for (int k1 = 0; k1 < M; k1++)
                for (int k2 = 0; k2 < M; k2++) {
                    ComplexVector s00MulOmega = s00[k1][k2];
                    ComplexVector s01MulOmega = s01[k1][k2].mul(omegaInverse(N, k2));
                    ComplexVector s10MulOmega = s10[k1][k2].mul(omegaInverse(N, k1));
                    ComplexVector s11MulOmega = s11[k1][k2].mul(omegaInverse(N, k1 + k2));
                    res[k1][k2] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                    res[k1][k2 + M] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .sumHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .sumHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .subHere(s11MulOmega);
                    res[k1 + M][k2 + M] = ComplexVector.zero()
                            .sumHere(s00MulOmega)
                            .subHere(s01MulOmega)
                            .subHere(s10MulOmega)
                            .sumHere(s11MulOmega);
                }
            return res;
        }
    }

    public static ComplexNumber omegaForward(int N, int pow) {
        double re, im;
        re = Math.cos(2 * pow * Math.PI / N);
        im = Math.sin(-2 * pow * Math.PI / N);
        return new ComplexNumber(re, im);
    }

    public static ComplexNumber omegaForwardApril(int N, int pow) {
        double x = -2 * Math.PI * pow / N;
        return new ComplexNumber(Math.cos(x), Math.sin(x));
    }

    public static ComplexNumber omegaInverse(int N, int pow) {
        double re, im;
        re = Math.cos(2 * pow * Math.PI / N);
        im = Math.sin(2 * pow * Math.PI / N);
        return new ComplexNumber(re, im);
    }

    public FourierImage deGradientWithCenter() {
        int N = width;
        int center = width / 2;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = new ComplexNumber(0, 2 * Math.PI);
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                double ky = center - y;
                double kx = x - center;
                if (ky == 0 && kx == 0) {
                    res[y][x] = new ComplexNumber(0, 0);
                    continue;
                }
                ComplexNumber temp = pixels[y][x].scalarMul(new ComplexVector(ky, kx))
//                ComplexNumber temp = new ComplexVector(y, x).scalarMul(pixels[x][y])
                        .div(twoPiImUnit)
                        .divByNumber(ky * ky + kx * kx);
                res[y][x] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

    public MagniImage[] magnitude() {
        MagniImage[] res = new MagniImage[2];
        MagniPixel[][] resPixels1 = new MagniPixel[width][width];
        MagniPixel[][] resPixels2 = new MagniPixel[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++) {
                resPixels1[y][x] = new MagniPixel(pixels[y][x].n1.length());
                resPixels2[y][x] = new MagniPixel(pixels[y][x].n2.length());
            }
        res[0] = new MagniImage(resPixels1);
        res[1] = new MagniImage(resPixels2);
        return res;
    }

    public static ComplexNumber[] omegasArrayForward(int N) {
        ComplexNumber[] res = new ComplexNumber[2 * N * N];
        for (int i = 0; i < res.length; i++)
            res[i] = omegaForward(N, i);
        return res;
    }

    public FourierImage getImage1() {
        ComplexNumber[][] res = new ComplexNumber[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = pixels[y][x].n1;
        return new FourierImage(res);
    }

    public FourierImage getImage2() {
        ComplexNumber[][] res = new ComplexNumber[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = pixels[y][x].n2;
        return new FourierImage(res);
    }
}