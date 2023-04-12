package org.kil0bait.magnifier.vector;

import org.kil0bait.magnifier.base.*;

public class ComplexVectorImage {
    private final ComplexVector[][] vectors;
    private final int height;
    private final int width;

    public ComplexVectorImage(MagniImage image1, MagniImage image2) {
        MagniException.validateEqualResolutions(image1, image2);
        height = image1.getHeight();
        width = image1.getWidth();
        vectors = new ComplexVector[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                vectors[y][x] = new ComplexVector(image1.pixel(y, x).getIntensity(), image2.pixel(y, x).getIntensity());
    }

    private ComplexVectorImage(ComplexVector[][] vectors) {
        this.vectors = vectors;
        this.height = vectors.length;
        this.width = vectors[0].length;
    }

    public ComplexVectorImage dftSlowForward() {
        ComplexVector[][] res = new ComplexVector[height][width];
        int centerY = height / 2;
        int centerX = width / 2;
        for (int k1 = 0; k1 < height; k1++) {
            for (int k2 = 0; k2 < width; k2++) {
                ComplexVector temp = ComplexVector.zero();
                for (int n1 = 0; n1 < height; n1++)
                    for (int n2 = 0; n2 < width; n2++)
                        temp.sumHere(vectors[n1][n2].mul(omegaForward(height,
                                (centerY - k1) * (centerY - n1) + (k2 - centerX) * (n2 - centerX))));
                res[k1][k2] = temp;
            }
        }
        return new ComplexVectorImage(res);
    }

    public ComplexVectorImage fftCooleyForward() {
        MagniException.validateHeightEqualsWidth(height, width);
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

    public ComplexVector[][] fftCooleyForwardRecursion(int N, int delta, int shift1, int shift2) {
        if (N == 1) {
            return new ComplexVector[][]{{vectors[shift1][shift2]}};
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

    public static ComplexNumber omegaForward(int N, int pow) {
        double x = -2 * Math.PI * pow / N;
        return new ComplexNumber(Math.cos(x), Math.sin(x));
    }

    public FourierImage integratedCombine() {
        int centerY = height / 2;
        int centerX = width / 2;
        ComplexNumber[][] res = new ComplexNumber[height][width];
        ComplexNumber twoPiImUnit = new ComplexNumber(0, 2 * Math.PI);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ky = centerY - y;
                double kx = x - centerX;
                if (ky == 0 && kx == 0) {
                    res[y][x] = new ComplexNumber(0, 0);
                    continue;
                }
                res[y][x] = vectors[y][x].scalarMul(new ComplexVector(ky, kx))
                        .div(twoPiImUnit)
                        .divByNumber(ky * ky + kx * kx);
            }
        }
        return new FourierImage(res);
    }

    public FourierImage differentialCombine() {
        int centerY = height / 2;
        int centerX = width / 2;
        ComplexNumber[][] res = new ComplexNumber[height][width];
        ComplexNumber twoPiImUnit = new ComplexNumber(0, 2 * Math.PI);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ky = centerY - y;
                double kx = x - centerX;
                if (ky == 0 && kx == 0) {
                    res[y][x] = new ComplexNumber(0, 0);
                    continue;
                }
                res[y][x] = vectors[y][x].scalarMul(new ComplexVector(ky, kx))
                        .mul(twoPiImUnit);
            }
        }
        return new FourierImage(res);
    }

    public MagniImage[] spectrumFourier() {
        MagniImage[] res = new MagniImage[2];
        MagniPixel[][] resPixels1 = new MagniPixel[width][width];
        MagniPixel[][] resPixels2 = new MagniPixel[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++) {
                resPixels1[y][x] = new MagniPixel(vectors[y][x].n1.length());
                resPixels2[y][x] = new MagniPixel(vectors[y][x].n2.length());
            }
        res[0] = new MagniImage(resPixels1);
        res[1] = new MagniImage(resPixels2);
        return res;
    }

    public FourierImage getImage1() {
        ComplexNumber[][] res = new ComplexNumber[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = vectors[y][x].n1;
        return new FourierImage(res);
    }

    public FourierImage getImage2() {
        ComplexNumber[][] res = new ComplexNumber[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                res[y][x] = vectors[y][x].n2;
        return new FourierImage(res);
    }
}
