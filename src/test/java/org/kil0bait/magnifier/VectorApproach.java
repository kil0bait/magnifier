package org.kil0bait.magnifier;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.kil0bait.magnifier.base.FourierImage;
import org.kil0bait.magnifier.base.MagniImage;
import org.kil0bait.magnifier.base.MagniPixel;
import org.kil0bait.magnifier.vector.ComplexVectorImage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VectorApproach {
    public static final Pattern VELOX_META_PATTERN = Pattern.compile("\"label\": \"([^\"]*)\".*\"dataPath\": \"([^\"]*)\"", Pattern.DOTALL);
    public static final String[] DET_SEQ = {"DF4-A", "DF4-B", "DF4-C", "DF4-D"};
    public static final String[] VELOX_SEQ = {"DF4-A", "DF4-D", "DF4-C", "DF4-B"};
    public static final String[] BOOK_SEQ = {"DF4-D", "DF4-C", "DF4-B", "DF4-A"};

    public static void main(String[] args) throws IOException {
        String folder = "aprilBor";
        MagniImage[] images = getImagesFromPath(folder, DET_SEQ);
//        MagniImage[] images = getImagesFromVelox(folder + "/in.emd", DET_SEQ);
        idpc(images, folder);
//        combinations(folder, DET_SEQ, ".png");
    }


    private static void idpc(MagniImage[] images, String folder) throws IOException {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        TestUtils.saveMagniImageWithName(imageX.dynamicNorm(), folder, "imageX");
        TestUtils.saveMagniImageWithName(imageY.dynamicNorm(), folder, "imageY");

        ComplexVectorImage function = new ComplexVectorImage(imageY, imageX);
        ComplexVectorImage fftForward = function.fftCooleyForward();
//        ComplexVectorImage fftForward = function.dftSlowForward();
//        fftForward.getImage2().saveToFile(new File("out/" + folder + "/fft_imageX_vector.txt"));
        MagniImage[] magnitudes = fftForward.spectrumFourier();

        TestUtils.saveMagniImageWithName(magnitudes[0].dynamicNormAverage(), folder, "magnitude1");
        TestUtils.saveMagniImageWithName(magnitudes[1].dynamicNormAverage(), folder, "magnitude2");

//        FourierImage integrated = fftForward.integrated();
        FourierImage integrated = fftForward.integratedCombine();
//        integrated.shift();
        FourierImage fourierImage = integrated
                .fftCooleyInverse();
//                .dftSlowInverseWithCenter();

        MagniImage idpcRe = fourierImage.imageFromRe().dynamicNorm();
        TestUtils.saveMagniImageWithName(idpcRe, folder, "idpc_RE");
//        MagniImage idpcIm = fourierImage.imageFromIm().dynamicNorm();
//        saveMagniImageWithName(idpcIm, folder, "idpc_ZIM");

        FourierImage differentialCombine = fftForward.differentialCombine().fftCooleyInverse();

        TestUtils.saveMagniImageWithName(differentialCombine.imageFromRe().dynamicNorm(), folder, "dDPC_RE");
    }


    private static void combinations(String folder, String[] names, String ext) {
        if (names.length != 4)
            return;
        int[][] cycles = {{0, 1, 2, 3}, {0, 2, 1, 3}, {0, 1, 3, 2}};
        MagniImage[] images = new MagniImage[]{
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, names[0] + ext))),
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, names[1] + ext))),
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, names[2] + ext))),
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, names[3] + ext)))
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
        TestUtils.createFolderIn(TestUtils.buildPath(folder, name));
        MagniImage imageRe = calculateIdpc(images).imageFromRe().dynamicNorm();
        TestUtils.saveMagniImageWithName(imageRe, folder, name);
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

    private static FourierImage calculateIdpc(MagniImage[] images) {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        FourierImage integratedCombine = new ComplexVectorImage(imageY, imageX).fftCooleyForward().integratedCombine();
        return integratedCombine.fftCooleyInverse();
    }

    public static MagniImage[] getImagesFromVelox(String hdfFilePath, String[] detSeq) {
        MagniImage[] res = new MagniImage[4];
        List<MagniImage> veloxImages = dpcImagesFromVelox(hdfFilePath);
        for (MagniImage image : veloxImages)
            for (int i = 0; i < detSeq.length; i++) {
                if (detSeq[i].equals(image.getName())) {
                    res[i] = image;
                    break;
                }
            }
        return res;
    }

    public static List<MagniImage> dpcImagesFromVelox(String hdfFilePath) {
        List<MagniImage> res = new ArrayList<>();
        try (HdfFile in = new HdfFile(Paths.get(TestUtils.buildPath("in", hdfFilePath)))) {
            Group detectorImages = (Group) in.getByPath("/Presentation/Displays/ImageDisplay");
            for (Map.Entry<String, Node> entry : detectorImages.getChildren().entrySet()) {
                String[] data = (String[]) ((Dataset) entry.getValue()).getData();
                Matcher matcher = VELOX_META_PATTERN.matcher(data[0]);
                if (matcher.find()) {
                    String label = matcher.group(1);
                    String imagePath = matcher.group(2).replaceAll("\\\\", "");
                    Dataset dataset = in.getDatasetByPath(imagePath + "/Data");
                    MagniImage image = imageFromDataset(dataset);
                    image.setName(label);
                    res.add(image);
                }
            }
        }
        return res;
    }

    public static MagniImage imageFromDataset(Dataset dataset) {
        MagniPixel[][] res = null;
        if (dataset.getData() instanceof int[][][] array) {
            res = new MagniPixel[array.length][array[0].length];
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    res[i][j] = new MagniPixel(array[i][j][0]);
                }
            }
        } else if (dataset.getData() instanceof float[][][] array) {
            res = new MagniPixel[array.length][array[0].length];
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    res[i][j] = new MagniPixel(array[i][j][0]);
                }
            }
        }
        return new MagniImage(res);
    }

    public static MagniImage[] getImagesFromPath(String folder, String[] detSeq) {
        String ext = ".png";
        return new MagniImage[]{
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, detSeq[0] + ext))),
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, detSeq[1] + ext))),
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, detSeq[2] + ext))),
                new MagniImage(TestUtils.loadImageFromOut(TestUtils.buildPath(folder, detSeq[3] + ext)))
        };
    }
}
