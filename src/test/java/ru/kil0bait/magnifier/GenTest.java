package ru.kil0bait.magnifier;

import ru.kil0bait.magnifier.classes.FourierImage;
import ru.kil0bait.magnifier.classes.FourierPair;
import ru.kil0bait.magnifier.classes.MagniImage;
import ru.kil0bait.magnifier.classes.Utils;

import static ru.kil0bait.magnifier.TestUtils.*;

public class GenTest {
    public static void main(String[] args) {
//        absoluteRandomImageTest("generated", 256);
//        randomFromImageTest("generatedFrom", "source", 0.001f);
        randomFromImagesIdpcTest("randomizedFromSource", new String[]{"A", "D", "C", "B"}, 0.003f, 255);
    }

    private static void absoluteRandomImageTest(String folder, int resolution) {
        MagniImage[] sourceImages = new MagniImage[4];
        for (int i = 0; i < 4; i++) {
            sourceImages[i] = Utils.generateImage(resolution);
            saveMagniImageToFolder(sourceImages[i].dynamicNorm(), folder, "det" + i + "_norm");
        }
        FourierImage sourceIdpc = calculateIdpc(sourceImages);
        saveMagniImageToFolder(sourceIdpc.getImageFromRe().dynamicNorm(), folder, "IDPC_source");

        MagniImage[] normImages = new MagniImage[4];
        for (int i = 0; i < 4; i++)
            normImages[i] = new MagniImage(loadPngImageFromOut(buildPath(folder, "det" + i + "_norm")));
        FourierImage normIdpc = calculateIdpc(normImages);
        saveMagniImageToFolder(normIdpc.getImageFromRe().dynamicNorm(), folder, "IDPC_norm");
    }

    private static void randomFromImageTest(String folder, String sourceImageName, float percent) {
        MagniImage image = new MagniImage(loadPngImageFromOut(buildPath(folder, sourceImageName)));

        MagniImage[] sourceImages = new MagniImage[4];
        for (int i = 0; i < 4; i++) {
            sourceImages[i] = Utils.generateImageFrom(image, percent);
            saveMagniImageToFolder(sourceImages[i].dynamicNorm(), folder, "det" + i + "_norm");
        }
        FourierImage sourceIdpc = calculateIdpc(sourceImages);
        saveMagniImageToFolder(sourceIdpc.getImageFromRe().dynamicNorm(), folder, "IDPC_source");

        MagniImage[] normImages = new MagniImage[4];
        for (int i = 0; i < 4; i++)
            normImages[i] = new MagniImage(loadPngImageFromOut(buildPath(folder, "det" + i + "_norm")));
        FourierImage normIdpc = calculateIdpc(normImages);
        saveMagniImageToFolder(normIdpc.getImageFromRe().dynamicNorm(), folder, "IDPC_norm");
    }

    private static void randomFromImagesIdpcTest(String folder, String[] names, float absolutePercent, float absoluteLimit) {
        MagniImage[] sourceImages = new MagniImage[4];
        for (int i = 0; i < 4; i++)
            sourceImages[i] = new MagniImage(loadPngImageFromOut(buildPath(folder, names[i])));
        saveMagniImageToFolder(calculateIdpc(sourceImages).getImageFromRe().dynamicNorm(), folder, "Idpc_source");

        MagniImage[] randomizedImages = new MagniImage[4];
        for (int i = 0; i < 4; i++) {
            randomizedImages[i] = Utils.generateAbsoluteImageFrom(sourceImages[i], absolutePercent, absoluteLimit);
            saveMagniImageToFolder(randomizedImages[i].dynamicNorm(), folder, "randomized" + i + "_norm");
        }
        FourierImage randomizedIdpc = calculateIdpc(randomizedImages);
        saveMagniImageToFolder(randomizedIdpc.getImageFromRe().dynamicNorm(), folder, "Idpc_randomized");
    }

    private static FourierImage calculateIdpc(MagniImage[] images) {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        FourierImage deGradient = fourierPair.deGradientDefault();
        return deGradient.fftCooleyInverse();
    }
}
