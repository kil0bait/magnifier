package ru.kil0bait.magnifier;

import boofcv.abst.transform.fft.DiscreteFourierTransform;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.misc.PixelMath;
import boofcv.alg.transform.fft.DiscreteFourierTransformOps;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ImageGridPanel;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.InterleavedF32;

import java.awt.image.BufferedImage;

import static ru.kil0bait.magnifier.TestUtils.buildPath;

public class BoofCV {

    public static void main(String[] args) {
//        GrayF32 input = UtilImageIO.loadImage("out/boofcv/imageX.png", GrayF32.class);
//        applyBoxFilter(input.clone());
        String folder = "boofcv3";
//        GrayF32[] images = detectorsImages(folder, "",".tif");
        GrayF32[] images = detectorsImages(folder, "DF4-", ".png");
        GrayF32 imageX = subtract(images[0], images[2]);
        GrayF32 imageY = subtract(images[1], images[3]);
        printFirstValues(imageX, 16);
        saveImage(dynamicNorm(imageX), folder, "imageX_calc");
        saveImage(dynamicNorm(imageY), folder, "imageY_calc");
//        InterleavedF32 fourier = fourier(imageX);
//        printFirstValues(fourier, 16);
//        System.out.printf("(%f, %fi)\r\n", fourier.getBand(0, 1, 0), fourier.getBand(0, 1, 1));
//        System.out.println(Arrays.toString(fourier.get(0, 1, null)));
        InterleavedF32 compound = compound(imageX, imageY);
        GrayF32 idpc = fourierInverse(compound);
        saveImage(idpc, folder, "idpc");
        saveImage(dynamicNorm(idpc), folder, "idpc_norm");
    }

    public static InterleavedF32 compound(GrayF32 imageX, GrayF32 imageY) {
        int N = imageX.height;
        InterleavedF32 res = new InterleavedF32(N, N, 2);
        InterleavedF32 xFourier = fourierForward(imageX);
        InterleavedF32 yFourier = fourierForward(imageY);

        shift(xFourier);
        shift(yFourier);

//        GaussElement twoPiImUnit = GaussElement.imaginaryUnit().mul(new GaussElement((float) (Math.PI * 2), 0));
        for (int ky = 0; ky < N; ky++) {
            for (int kx = 0; kx < N; kx++) {
                if (kx == 0 && ky == 0) {
                    res.set(kx, ky, 0, 0);
                    continue;
                }
                float realKY = (float) ky;
                float realKX = (float) kx;
                float[] temp = mulByFloat(conjugate(xFourier.get(kx, ky, null)), realKX);
                temp = sum(temp, mulComplex(conjugate(yFourier.get(kx, ky, null)), mulByImaginaryUnit(complexFromReal(realKY))));
                temp = mulByFloat(mulByImaginaryUnit(temp), -1);
                temp = divByFloat(temp, (float) Math.PI * 2);
                temp = divByFloat(temp, realKX * realKX + realKY * realKY);
                res.set(kx, ky, temp);
            }
        }

        shift(res);
        return res;
    }

    public static float[]  mulComplex(float[] thiz, float[] that) {
        float[] res = new float[2];
        res[0] = thiz[0] * that[0] - thiz[1] * that[1];
        res[1] = thiz[0] * that[1] + thiz[1] * that[0];
        return res;
    }

    public static float[] complexFromReal(float re) {
        float[] res = new float[2];
        res[0] = re;
        res[1] = 0;
        return res;
    }

    public static float[] mulByFloat(float[] array, float m) {
        float[] res = new float[array.length];
        for (int i = 0; i < res.length; i++)
            res[i] = array[i] * m;
        return res;
    }

    public static float[] mulByImaginaryUnit(float[] array) {
        float[] res = swap(array);
        res[0] *= (-1);
        return res;
    }

    public static float[] divByFloat(float[] array, float d) {
        float[] res = new float[array.length];
        for (int i = 0; i < res.length; i++)
            res[i] = array[i] / d;
        return res;
    }

    public static float[] sum(float[] array1, float[] array2) {
        float[] res = new float[array1.length];
        for (int i = 0; i < res.length; i++)
            res[i] = array1[i] + array2[i];
        return res;
    }

    private static float[] conjugate(float[] array) {
        float[] res = new float[array.length];
        res[0] = array[0];
        res[1] = -array[1];
        return res;
    }

    private static float[] swap(float[] array) {
        float[] res = new float[array.length];
        res[0] = array[1];
        res[1] = array[0];
        return res;
    }

    public static void shift(InterleavedF32 image) {
        int N = image.height;
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                if ((x + y) % 2 == 0)
                    image.set(x, y, mulByFloat(image.get(x, y, null), -1));
            }
        }
    }

//    public static InterleavedF32 fourierForward4d(GrayF32 input1, GrayF32 input2){
//        DiscreteFourierTransform<GrayF32, InterleavedF32> dft =
//                DiscreteFourierTransformOps.createTransformF32();
//        InterleavedF32 transform = new InterleavedF32(input.width, input.height, 4);
//        dft.forward(input, transform);
//        return transform;
//    }

    public static InterleavedF32 fourierForward(GrayF32 input) {
        DiscreteFourierTransform<GrayF32, InterleavedF32> dft =
                DiscreteFourierTransformOps.createTransformF32();
        InterleavedF32 transform = new InterleavedF32(input.width, input.height, 2);
        dft.forward(input, transform);
        return transform;
    }

    public static GrayF32 fourierInverse(InterleavedF32 input) {
        GrayF32 res = new GrayF32(input.width, input.height);
        DiscreteFourierTransform<GrayF32, InterleavedF32> dft =
                DiscreteFourierTransformOps.createTransformF32();
        dft.inverse(input, res);
        return res;
    }

    public static GrayF32 subtract(GrayF32 image1, GrayF32 image2) {
        int N = image1.height;
        GrayF32 res = new GrayF32(N, N);
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++)
                res.set(x, y, image1.get(x, y) - image2.get(x, y));
        return res;
    }

    public static GrayF32[] detectorsImages(String folderName, String pattern, String ext) {
        GrayF32[] res = new GrayF32[4];
        for (int i = 0; i < 4; i++)
            res[i] = UtilImageIO.loadImage(buildPath("out", folderName, pattern + i + ext), GrayF32.class);
        return res;
    }

    private static void saveImage(GrayF32 image, String folder, String name) {
        UtilImageIO.saveImage(image, buildPath("out", folder, name + ".png"));
    }


    public static GrayF32 dynamicNorm(GrayF32 input) {
        int N = input.height;
        GrayF32 res = new GrayF32(N, N);
        float min = 0, max = 0;
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                min = Math.min(input.get(x, y), min);
                max = Math.max(input.get(x, y), max);
            }
        System.out.println("Min = " + min + "; Max = " + max);
        max -= min;
        for (int y = 0; y < N; y++)
            for (int x = 0; x < N; x++) {
                float t = input.get(x, y);
                t = ((t - min) / max) * 255;
                res.set(x, y, t);
            }
        return res;
    }

    private static void printFirstValues(GrayF32 image, int size) {
        System.out.println("-----Start-----");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++)
                System.out.printf("%f    ", image.get(x, y));
            System.out.println();
        }
        System.out.println("-----END-----");
    }

    private static void printFirstValues(InterleavedF32 image, int size) {
        System.out.println("-----Start-----");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++)
                System.out.printf("(%f, %fi)    ", image.getBand(x, y, 0), image.getBand(x, y, 1));
            System.out.println();
        }
        System.out.println("-----END-----");
    }

    public static void applyBoxFilter(GrayF32 input) {
        // declare storage
        GrayF32 boxImage = new GrayF32(input.width, input.height);
        InterleavedF32 boxTransform = new InterleavedF32(input.width, input.height, 2);
        InterleavedF32 transform = new InterleavedF32(input.width, input.height, 2);
        GrayF32 blurredImage = new GrayF32(input.width, input.height);
        GrayF32 spatialBlur = new GrayF32(input.width, input.height);

        DiscreteFourierTransform<GrayF32, InterleavedF32> dft =
                DiscreteFourierTransformOps.createTransformF32();

        // Make the image scaled from 0 to 1 to reduce overflow issues
        PixelMath.divide(input, 255.0f, input);

        // compute the Fourier Transform
        dft.forward(input, transform);

        // create the box filter which is centered around the pixel. Note that the filter gets wrapped around
        // the image edges
        for (int y = 0; y < 15; y++) {
            int yy = y - 7 < 0 ? boxImage.height + (y - 7) : y - 7;
            for (int x = 0; x < 15; x++) {
                int xx = x - 7 < 0 ? boxImage.width + (x - 7) : x - 7;
                // Set the value such that it doesn't change the image intensity
                boxImage.set(xx, yy, 1.0f / (15 * 15));
            }
        }

        // compute the DFT for the box filter
        dft.forward(boxImage, boxTransform);

        // Visualize the Fourier Transform for the input image and the box filter
        displayTransform(transform, "Input Image");
        displayTransform(boxTransform, "Box Filter");

        // apply the filter. convolution in spacial domain is the same as multiplication in the frequency domain
        DiscreteFourierTransformOps.multiplyComplex(transform, boxTransform, transform);

        // convert the image back and display the results
        dft.inverse(transform, blurredImage);
        // undo change of scale
        PixelMath.multiply(blurredImage, 255.0f, blurredImage);
        PixelMath.multiply(input, 255.0f, input);

        // For sake of comparison, let's compute the box blur filter in the spatial domain
        // NOTE: The image border will be different since the frequency domain wraps around and this implementation
        // of the spacial domain adapts the kernel size
        BlurImageOps.mean(input, spatialBlur, 7, null, null);

        // Convert to BufferedImage for output
        BufferedImage originOut = ConvertBufferedImage.convertTo(input, null);
        BufferedImage spacialOut = ConvertBufferedImage.convertTo(spatialBlur, null);
        BufferedImage blurredOut = ConvertBufferedImage.convertTo(blurredImage, null);

        ListDisplayPanel listPanel = new ListDisplayPanel();
        listPanel.addImage(originOut, "Original Image");
        listPanel.addImage(spacialOut, "Spacial Domain Box");
        listPanel.addImage(blurredOut, "Frequency Domain Box");

        ShowImages.showWindow(listPanel, "Box Blur in Spacial and Frequency Domain of Input Image");
    }

    public static void displayTransform(InterleavedF32 transform, String name) {
        // declare storage
        GrayF32 magnitude = new GrayF32(transform.width, transform.height);
        GrayF32 phase = new GrayF32(transform.width, transform.height);

        // Make a copy so that you don't modify the input
        transform = transform.clone();

        // shift the zero-frequency into the image center, as is standard in image processing
        DiscreteFourierTransformOps.shiftZeroFrequency(transform, true);

        // Compute the transform's magnitude and phase
        DiscreteFourierTransformOps.magnitude(transform, magnitude);
        DiscreteFourierTransformOps.phase(transform, phase);

        // Convert it to a log scale for visibility
        PixelMath.log(magnitude, 1.0f, magnitude);

        // Display the results
        BufferedImage visualMag = VisualizeImageData.grayMagnitude(magnitude, null, -1);
        BufferedImage visualPhase = VisualizeImageData.colorizeSign(phase, null, Math.PI);

        ImageGridPanel dual = new ImageGridPanel(1, 2, visualMag, visualPhase);
        ShowImages.showWindow(dual, "Magnitude and Phase of " + name);
    }

}
