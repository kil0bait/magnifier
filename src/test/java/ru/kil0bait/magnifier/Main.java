package ru.kil0bait.magnifier;

import ru.kil0bait.magnifier.classes.FourierImage;
import ru.kil0bait.magnifier.classes.FourierPair;
import ru.kil0bait.magnifier.classes.MagniImage;

import java.io.IOException;

import static ru.kil0bait.magnifier.TestUtils.*;

public class Main {
    public static void main(String[] args) throws IOException {
//        dftTest("100");
//        mainTest("obg", "256obg");
//        mainTest("book256.1", "book256");
//        mainTest("book22", "book22");
//        mainTest("ivan1", "ivan1");
//        mainTest("book22", "book22");
//        mainTest("obg128", "obg128");
//        mainTest("obg127", "obg127");
//
//        dftTest("256OBG");
//        fftCooleyTest("256OBG");
//        dftTest("2x2");
//        fftCooleyTest("2x2");
//        fromXAndYTest("bookXY","bookXY");

//        combinations("cycle", new String[]{"A", "B", "C", "D"}, ".tif");
//        angles360("360_DCBA", new String[]{"D", "C", "B", "A"}, ".tif");
        usualIdpc("real", new String[]{"C", "D", "A", "B"}, ".tif");
//        usualIdpc("book2811", new String[]{"A", "B", "C", "D"}, ".png");
//        usualIdpc("usual", new String[]{"C", "D", "B", "A"}, ".tif");
//        idpsWithPrintValues("16x16", new String[]{"A4", "B4", "C4", "D4"}, ".png");
    }

    private static void idpsWithPrintValues(String folder, String[] names, String ext) {
        MagniImage[] images = new MagniImage[names.length];
        for (int i = 0; i < names.length; i++) {
            images[i] = new MagniImage(loadImageFromIn(buildPath(folder, names[i] + ext)));
            System.out.println(images[i].rawValues());
        }
        MagniImage imageX = images[0].subtract(images[2]);
        saveMagniImageToFolder(imageX, folder, "IDPC_x");
        System.out.println("IDPC_x = A4 - C4");
        System.out.println(imageX.rawValues());
        MagniImage imageY = images[1].subtract(images[3]);
        saveMagniImageToFolder(imageY, folder, "IDPC_y");
        System.out.println("IDPC_y = B4 - D4");
        System.out.println(imageY.rawValues());
        FourierImage fourierImageX = new FourierImage(imageX).fftCooleyForward();
        FourierImage fourierImageY = new FourierImage(imageY).fftCooleyForward();
        System.out.println("Fourier forward of IDPC_x\r\n" + fourierImageX.rawValues());
        System.out.println("Fourier forward of IDPC_y\r\n" + fourierImageY.rawValues());
        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        FourierImage deGradient = fourierPair.deGradientDefault();
        System.out.println("DeGradient\r\n" + deGradient.rawValues());
        FourierImage result = deGradient.fftCooleyInverse();
        System.out.println("Result after Fourier inverse\r\n" + result.rawValues());
        saveMagniImageToFolder(result.getImageFromRe().dynamicNorm(), folder, "result_REAL_normalized");
        saveMagniImageToFolder(result.getImageFromIm().dynamicNorm(), folder, "result_IMAG_normalized");
    }

    private static void usualIdpc(String folder, String[] names, String ext) {
        MagniImage[] images = new MagniImage[]{
                new MagniImage(loadImageFromIn(buildPath(folder, names[0] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[1] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[2] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[3] + ext)))
        };
        MagniImage imageX = images[0].subtract(images[2]).dynamicNorm();
        MagniImage imageY = images[1].subtract(images[3]).dynamicNorm();
        saveMagniImageToFolder(imageX, folder, "imageX");
        saveMagniImageToFolder(imageY, folder, "imageY");
        FourierImage fourierImage = calculateIdpc(images);
        saveMagniImageToFolder(fourierImage.magnitude().dynamicNorm(),folder,"magnitude");
        MagniImage idpcRe = fourierImage.getImageFromRe().dynamicNorm();
        saveMagniImageToFolder(idpcRe, folder, "idpc_RE");
        MagniImage idpcIm = fourierImage.getImageFromIm().dynamicNorm();
        saveMagniImageToFolder(idpcIm, folder, "idpc_ZIM");
    }

    private static FourierImage calculateIdpc(MagniImage[] images) {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        FourierImage deGradient = fourierPair.deGradientDefault();
        return deGradient.fftCooleyInverse();
    }

    private static void angles360(String folder, String[] names, String ext) {
        MagniImage[] images = new MagniImage[]{
                new MagniImage(loadImageFromIn(buildPath(folder, names[0] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[1] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[2] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[3] + ext)))
        };
        String name = names[0] + names[1] + names[2] + names[3];
        calculateIdpc360(images, "idpc" + name, folder);
    }

    private static void combinations(String folder, String[] names, String ext) {
        if (names.length != 4)
            return;
        int[][] cycles = {{0, 1, 2, 3}, {0, 2, 1, 3}, {0, 1, 3, 2}};
        MagniImage[] images = new MagniImage[]{
                new MagniImage(loadImageFromIn(buildPath(folder, names[0] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[1] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[2] + ext))),
                new MagniImage(loadImageFromIn(buildPath(folder, names[3] + ext)))
        };
        for (int[] cycle : cycles) {
            String postfix = concatByOrder(names, cycle);
            cycleAction(cycle, images, names, postfix, folder);
            cycleAction(reversed(cycle), images, names, postfix + "_reverse", folder);
        }
    }

    private static void cycleAction(int[] cycle, MagniImage[] images, String[] names, String postfix, String folder) {
        int mod = cycle.length;
        for (int i = 0; i < cycle.length; i++) {
            MagniImage[] ordered = new MagniImage[mod];
            StringBuilder name = new StringBuilder();
            for (int j = 0; j < cycle.length; j++) {
                ordered[j] = images[cycle[(i + j) % mod]];
                name.append(names[cycle[(i + j) % mod]]);
            }
            name.append("__").append(postfix);
            if (i != 0)
                name.append("_cycle");
            singleAction(ordered, name.toString(), folder);
        }
    }

    private static void singleAction(MagniImage[] images, String name, String folder) {
        createFolderIn(buildPath(folder, name));
        MagniImage imageRe = calculateIdpc(images).getImageFromRe().dynamicNorm();
        MagniImage imageIm = calculateIdpc(images).getImageFromIm().dynamicNorm();
        saveMagniImageToFolder(imageRe, folder, name + "_RE");
        saveMagniImageToFolder(imageIm, folder, name + "_IM");
    }

    private static String concatByOrder(String[] s, int[] order) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.length; i++)
            sb.append(s[order[i]]);
        return sb.toString();
    }

    private static int[] reversed(int[] array) {
        int length = array.length;
        int[] res = new int[length];
        for (int i = 0; i < length; i++)
            res[i] = array[length - 1 - i];
        return res;
    }


    private static void calculateIdpc360(MagniImage[] images, String name, String folder) {
        for (int i = 0; i < 360; i++) {
            int angle = (30 + i) % 360;
            MagniImage imageX = imageX(images, angle);
            MagniImage imageY = imageY(images, angle);
            FourierPair fourierPair = new FourierPair(imageX, imageY);
            fourierPair.fftCooleyForward();
            FourierImage deGradient = fourierPair.deGradientWithRotation(0);
            FourierImage idpcInverse = deGradient.fftCooleyInverse();
            MagniImage image = idpcInverse.getImageFromRe().dynamicNorm();
            saveMagniImageToFolder(image, folder, name + "_" + angle);
        }
    }


    private static MagniImage imageX(MagniImage[] images, float angle) {
        double angleInRadians = Math.toRadians(angle);
        MagniImage image = images[0].mulByNumber((float) Math.cos(angleInRadians));
        for (int i = 1; i < 4; i++)
            image = image.sum(images[i].mulByNumber((float) Math.cos(angleInRadians + Math.PI * i / 2)));
        return image;
    }

    private static MagniImage imageY(MagniImage[] images, float angle) {
        double angleInRadians = Math.toRadians(angle);
        MagniImage image = images[0].mulByNumber((float) Math.sin(angleInRadians));
        for (int i = 1; i < 4; i++)
            image = image.sum(images[i].mulByNumber((float) Math.sin(angleInRadians + Math.PI * i / 2)));
        return image;
    }

    private static void mainTest(String folder, String name) {
        MagniImage[] images = new MagniImage[4];
        for (int i = 0; i < 4; i++)
            images[i] = new MagniImage(loadPngImageFromIn(folder + "/" + i));
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        saveMagniImageToFolder(imageX.dynamicNorm(), folder, name + "_X_axis");
        saveMagniImageToFolder(imageY.dynamicNorm(), folder, name + "_Y_axis");

        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        for (int i = 0; i < 8; i++) {
            FourierImage deGradient = fourierPair.deGradientWithRotation(i);
            FourierImage idpcOutInverse = deGradient.fftCooleyInverse();
            FourierImage gradient = fourierPair.gradientWithRotation(i);
            FourierImage ddpcOutInverse = gradient.fftCooleyInverse();
            FourierImage ddpcFromIdpc = FourierImage.ddpcFromIdpc(deGradient).fftCooleyInverse();

            saveMagniImageToFolder(idpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_" + i + "_idpc_Re");
            saveMagniImageToFolder(idpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_" + i + "_idpc_Im");
            saveMagniImageToFolder(idpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_" + i + "_idpc_Le");

            saveMagniImageToFolder(ddpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_" + i + "_ddpc_Re");
            saveMagniImageToFolder(ddpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_" + i + "_ddpc_Im");
            saveMagniImageToFolder(ddpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_" + i + "_ddpc_Le");

            saveMagniImageToFolder(ddpcFromIdpc.getImageFromRe().dynamicNorm(), folder, name + "_x_" + i + "_ddpcFI_Re");
            saveMagniImageToFolder(ddpcFromIdpc.getImageFromIm().dynamicNorm(), folder, name + "_x_" + i + "_ddpcFI_Im");
        }

        FourierImage deGradient = fourierPair.deGradientDefaultWithK();
        FourierImage idpcOutInverse = deGradient.fftCooleyInverse();

        saveMagniImageToFolder(idpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_DEFAULT_idpc_Re");
        saveMagniImageToFolder(idpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_DEFAULT_idpc_Im");
        saveMagniImageToFolder(idpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_DEFAULT_idpc_Le");

        FourierImage gradient = fourierPair.gradientDefaultWithK();
        FourierImage ddpcOutInverse = gradient.fftCooleyInverse();

        saveMagniImageToFolder(ddpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_DEFAULT_ddpc_Re");
        saveMagniImageToFolder(ddpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_DEFAULT_ddpc_Im");
        saveMagniImageToFolder(ddpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_DEFAULT_ddpc_Le");


    }

    private static void fromXAndYTest(String folder, String name) {
        MagniImage imageX = new MagniImage(loadPngImageFromIn(folder + "/" + "dpcX"));
        MagniImage imageY = new MagniImage(loadPngImageFromIn(folder + "/" + "dpcY"));
        saveMagniImageToFolder(imageX.dynamicNorm(), folder, name + "_X_axis");
        saveMagniImageToFolder(imageY.dynamicNorm(), folder, name + "_Y_axis");

        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        for (int i = 0; i < 8; i++) {
            FourierImage deGradient = fourierPair.deGradientWithRotation(i);
            FourierImage idpcOutInverse = deGradient.fftCooleyInverse();
            FourierImage gradient = fourierPair.gradientWithRotation(i);
            FourierImage ddpcOutInverse = gradient.fftCooleyInverse();

            saveMagniImageToFolder(idpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_" + i + "_idpc_Re");
            saveMagniImageToFolder(idpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_" + i + "_idpc_Im");
            saveMagniImageToFolder(idpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_" + i + "_idpc_Le");

            saveMagniImageToFolder(ddpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_" + i + "_ddpc_Re");
            saveMagniImageToFolder(ddpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_" + i + "_ddpc_Im");
            saveMagniImageToFolder(ddpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_" + i + "_ddpc_Le");
        }

        FourierImage deGradient = fourierPair.deGradientDefaultWithK();
        FourierImage idpcOutInverse = deGradient.fftCooleyInverse();

        saveMagniImageToFolder(idpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_DEFAULT_idpc_Re");
        saveMagniImageToFolder(idpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_DEFAULT_idpc_Im");
        saveMagniImageToFolder(idpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_DEFAULT_idpc_Le");

        FourierImage gradient = fourierPair.gradientDefaultWithK();
        FourierImage ddpcOutInverse = gradient.fftCooleyInverse();

        saveMagniImageToFolder(ddpcOutInverse.getImageFromRe().dynamicNorm(), folder, name + "_DEFAULT_ddpc_Re");
        saveMagniImageToFolder(ddpcOutInverse.getImageFromIm().dynamicNorm(), folder, name + "_DEFAULT_ddpc_Im");
        saveMagniImageToFolder(ddpcOutInverse.getImageFromLength().dynamicNorm(), folder, name + "_DEFAULT_ddpc_Le");

    }

    public static void dftTest(String name) {
        MagniImage image = new MagniImage(loadPngImageFromOut(name + "in"));
        saveBImageAsPng(image.toBufferedImage(), name + "in2");
        FourierImage fourierImage = new FourierImage(image);
        FourierImage dftForward = fourierImage.dftSlowForward();
        saveBImageAsPng(dftForward.magnitude().toBufferedImage(), name + "magn2");
        FourierImage dftInverse = dftForward.dftSlowInverse();
        MagniImage out = dftInverse.getImageFromRe();
        saveBImageAsPng(out.toBufferedImage(), name + "out2");
    }

    public static void fftCooleyTest(String name) {
        MagniImage image = new MagniImage(loadPngImageFromOut(name + "in"));
        saveBImageAsPng(image.toBufferedImage(), name + "in2");
        FourierImage fourierImage = new FourierImage(image);
        FourierImage fftForward = fourierImage.fftCooleyForward();
        saveBImageAsPng(fftForward.magnitude().toBufferedImage(), name + "magnCooley2");
        FourierImage fftInverse = fftForward.fftCooleyInverse();
        MagniImage out = fftInverse.getImageFromRe();
        saveBImageAsPng(out.toBufferedImage(), name + "outCooley2");
    }

}
