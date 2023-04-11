package ru.kil0bait.magnifier.pair;

import ru.kil0bait.magnifier.base.ComplexNumber;
import ru.kil0bait.magnifier.base.FourierImage;
import ru.kil0bait.magnifier.base.MagniImage;

import static ru.kil0bait.magnifier.base.FourierImage.omegasArrayForward;

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

    public FourierImage deGradientAprilWithCenter() {
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
                ComplexNumber temp = xPixels[y][x].mulByNumber(kx)
                        .sum(yPixels[y][x].mulByNumber(ky))
                        .div(twoPiImUnit)
                        .divByNumber(ky * ky + kx * kx);
                res[y][x] = new ComplexNumber(temp);
            }
        }
        return new FourierImage(res);
    }

}
