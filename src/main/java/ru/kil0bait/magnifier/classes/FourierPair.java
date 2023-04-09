package ru.kil0bait.magnifier.classes;

import static ru.kil0bait.magnifier.classes.FourierImage.omegasArrayForward;

public class FourierPair {
    private ComplexNumber[][] xPixels;
    private ComplexNumber[][] yPixels;
    private final int width;

    public FourierPair(MagniImage xImage, MagniImage yImage) {
        MagniImage.checkImagesResolutions(xImage, yImage);
        MagniImage.checkImageWidthEqualsHeight(xImage);
        width = xImage.getWidth();
        xPixels = new ComplexNumber[width][width];
        yPixels = new ComplexNumber[width][width];
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++) {
                xPixels[y][x] = new ComplexNumber(new ComplexNumber(xImage.pixel(y, x).getIntensity(), 0));
                yPixels[y][x] = new ComplexNumber(new ComplexNumber(yImage.pixel(y, x).getIntensity(), 0));
            }
    }

    public FourierImage getImageX() {
        return new FourierImage(xPixels);
    }

    public FourierImage getImageY() {
        return new FourierImage(yPixels);
    }

    @Deprecated
    private FourierPair(ComplexNumber[][] xPixels, ComplexNumber[][] yPixels) {
        this.width = xPixels.length;
        this.xPixels = xPixels;
        this.yPixels = yPixels;
    }

    public MagniImage magnitude() {
        return null;
    }

    public void dftSlowForward() {
        int N = width;
        ComplexNumber[][] resX = new ComplexNumber[N][N];
        ComplexNumber[][] resY = new ComplexNumber[N][N];
        ComplexNumber[] omegas = omegasArrayForward(N);
        for (int k1 = 0; k1 < N; k1++) {
            for (int k2 = 0; k2 < N; k2++) {
                ComplexNumber tempX = ComplexNumber.zero();
                ComplexNumber tempY = ComplexNumber.zero();
                for (int n1 = 0; n1 < N; n1++)
                    for (int n2 = 0; n2 < N; n2++) {
                        tempX.sumHere(omegas[k1 * n1 + k2 * n2].mul(xPixels[n1][n2]));
                        tempY.sumHere(omegas[k1 * n1 + k2 * n2].mul(yPixels[n1][n2]));
                    }
                resX[k1][k2] = new ComplexNumber(tempX);
                resY[k1][k2] = new ComplexNumber(tempY);
            }
        }
        xPixels = resX;
        yPixels = resY;
    }

    public void fftCooleyForward() {
        xPixels = new FourierImage(xPixels).fftCooleyForward().getPixels();
        yPixels = new FourierImage(yPixels).fftCooleyForward().getPixels();
    }

    public void shift() {
        for (int y = 0; y < width; y++)
            for (int x = 0; x < width; x++)
                if ((x + y) % 2 != 0) {
                    xPixels[y][x].mulByNumberHere(-1);
                    yPixels[y][x].mulByNumberHere(-1);
                }
    }

    public void shiftApril() {
        ComplexNumber[][] resX = new ComplexNumber[width][width];
        ComplexNumber[][] resY = new ComplexNumber[width][width];
        int center = width / 2;
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                resX[y][x] = xPixels[(y - center + width) % width][(x - center + width) % width];
                resY[y][x] = yPixels[(y - center + width) % width][(x - center + width) % width];
            }
        }
        xPixels = resX;
        yPixels = resY;
    }

    public FourierImage deGradientWithRotation(double angleMultiplier) {
        int N = width;
        double angle = Math.PI * angleMultiplier / 4;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                double realX = x * Math.cos(angle) - (-y) * Math.sin(angle);
                double realY = x * Math.sin(angle) + (-y) * Math.cos(angle);
                ComplexNumber temp = xPixels[y][x].mulByNumber(realX)
                        .sum(yPixels[y][x].mulByNumber(realY))
                        .div(twoPiImUnit)
                        .mul(new ComplexNumber(-1, 0));
                res[y][x] = new ComplexNumber(temp);
            }
        return new FourierImage(res);
    }

    public FourierImage gradientWithRotation(double angleMultiplier) {
        int N = width;
        double angle = Math.PI * angleMultiplier / 4;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        double xCenter = 0;
        double yCenter = 0;
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                double realX = (x - xCenter) * Math.cos(angle) - (-(y - yCenter)) * Math.sin(angle);
                double realY = (x - xCenter) * Math.sin(angle) + (-(y - yCenter)) * Math.cos(angle);
                double normRp = Math.pow(Math.pow(realX, 2) + Math.pow(realY, 2), 0.5);
                if (normRp == 0) {
                    res[y][x] = ComplexNumber.zero();
                    continue;
                }
                double kX = realX / normRp;
                double kY = realY / normRp;
                res[y][x] = (xPixels[y][x].mulByNumber(kX))
                        .sum(yPixels[y][x].mulByNumber(kY))
                        .mulHere(twoPiImUnit)
//                        .mulHere(new GaussElement(-1, 0))
                ;
            }
        return new FourierImage(res);
    }

    public FourierImage deGradientDefault() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        int center = width / 2;
        int limit = N / 2;
        for (int ky = 0; ky < N; ky++) {
            for (int kx = 0; kx < N; kx++) {
                double realKY = Math.PI * 2 / (ky - res.length);
                double realKX = Math.PI * 2 / (kx - center);
                if (ky == center || kx == center) {
                    res[ky][kx] = new ComplexNumber(0, 0);
                    continue;
                }
                ComplexNumber temp = xPixels[ky][kx].mulByNumber(realKX)
                        .sum(yPixels[ky][kx].mulByNumber(realKY))
                        .div(twoPiImUnit)
                        .divByNumber(realKX * realKX + realKY * realKY);
                res[ky][kx] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }


    public FourierImage deGradientApril() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                if (x == 0 && y == 0) {
                    res[0][0] = ComplexNumber.zero();
                    continue;
                }
                double normRp = x * x + y * y;
                res[y][x] =
                        (xPixels[y][x].mulByNumber(x))
                                .sum(yPixels[y][x].mulByNumber(y))
                                .div(twoPiImUnit)
                                .divByNumberHere(normRp)
                ;
            }
        return new FourierImage(res);
    }

    public FourierImage deGradientApril2() {
        int N = width;
        int center = width / 2;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                double ky = center - y;
                double kx = x - center;
                if (ky == 0 || kx == 0) {
                    res[y][x] = new ComplexNumber(0, 0);
                    continue;
                }
                ComplexNumber temp =
                        xPixels[y][x].mulByNumber(kx)
                                .sum(yPixels[y][x].mulByNumber(ky))
//                        pixels[y][x].scalarMul(new ComplexVector(ky, kx))
                                .div(twoPiImUnit)
                                .divByNumber(ky * ky + kx * kx);
                res[y][x] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

    public MagniImage deGradientDefaultDist() {
        int N = width;
        ComplexNumber[][] resY = new ComplexNumber[N][N];
        ComplexNumber[][] resX = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        int center = N / 2;
        for (int ky = 0; ky < N; ky++) {
            for (int kx = 0; kx < N; kx++) {
                double realKY = ky - center;
                double realKX = kx - center;
                if (ky == center)
                    resY[ky][kx] = new ComplexNumber(0, 0);
                else
                    resY[ky][kx] = yPixels[ky][kx].mulByNumber(realKY)
                            .div(twoPiImUnit)
                            .divByNumber(realKY * realKY);
                if (kx == center)
                    resX[ky][kx] = new ComplexNumber(0, 0);
                else
                    resX[ky][kx] = xPixels[ky][kx].mulByNumber(realKX)
                            .div(twoPiImUnit)
                            .divByNumber(realKX * realKX);
            }
        }
        MagniImage xReal = new FourierImage(resX).fftCooleyInverse().getImageFromIm();
        MagniImage yReal = new FourierImage(resY).fftCooleyInverse().getImageFromIm();
        MagniPixel[][] res = new MagniPixel[N][N];
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                double realY = yReal.pixel(y, x).getIntensity();
                double realX = xReal.pixel(y, x).getIntensity();
                res[y][x] = new MagniPixel(Math.sqrt(realY * realY + realX * realX));
            }
        }
        return new MagniImage(res);
    }

    public FourierImage deGradientWithK() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        for (int ky = 0; ky < N; ky++)
            for (int kx = 0; kx < N; kx++) {
//                if (kx == 0 && ky == 0) {
//                    res[ky][kx] = new GaussElement(10000,0);
//                    continue;
//                }
//                double k = N * kx + ky;
                ComplexNumber temp = xPixels[ky][kx]
                        .sum(yPixels[ky][kx])
//                        .div(new GaussElement(k, 0))
                        .div(twoPiImUnit);
                res[ky][kx] = new ComplexNumber(temp);
            }
        return new FourierImage(res);
    }

    public FourierImage gradientDefault() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        double xCenter = (N - 1) / 2.0F;
        double yCenter = (N - 1) / 2.0F;
        for (int x = 0; x < N; x++)
            for (int y = 0; y < N; y++) {
                double normRp = Math.pow(x - xCenter, 2) + Math.pow(yCenter - y, 2);
                double kX = (x - xCenter) / normRp;
                double kY = (yCenter - y) / normRp;
                res[y][x] = (xPixels[y][x].mulByNumber(kX))
                        .sum(yPixels[y][x].mulByNumber(kY))
                        .mulHere(twoPiImUnit);
            }
        return new FourierImage(res);
    }

    public FourierImage deGradientDefaultWithK() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
        ComplexNumber twoPiImUnit = ComplexNumber.imaginaryUnit().mul(new ComplexNumber(Math.PI * 2, 0));
        double xCenter = (N - 1) / 2.0F;
        double yCenter = (N - 1) / 2.0F;
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                double normRp = (x - xCenter) * (x - xCenter) + (yCenter - y) * (yCenter - y);
                double kX = (x - xCenter) / normRp;
                double kY = (yCenter - y) / normRp;
                double normKp = kX * kX + kY * kY;
                res[y][x] = (xPixels[y][x].mulByNumber(kX))
                        .sum(yPixels[y][x].mulByNumber(kY))
                        .div(twoPiImUnit)
                        .divByNumberHere(normKp);
            }
        return new FourierImage(res);
    }

    public FourierImage gradientDefaultWithK() {
        int N = width;
        ComplexNumber[][] res = new ComplexNumber[N][N];
//        GaussElement twoPiImUnit = GaussElement.imaginaryUnit().mul(new GaussElement(Math.PI * 2, 0));
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                if (x == 0 && y == 0) {
                    res[y][x] = ComplexNumber.zero();
                    continue;
                }
                double normRp = (x) * (x) + (y) * (y);
                double kX = (x) / normRp;
                double kY = (-y) / normRp;
                res[y][x] = (xPixels[y][x].mulByNumber(x))
                        .sum(yPixels[y][x].mulByNumber(-y))
//                        .mul(twoPiImUnit)
                ;
            }
        return new FourierImage(res);
    }

}
