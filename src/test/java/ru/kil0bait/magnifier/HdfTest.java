package ru.kil0bait.magnifier;

import io.jhdf.GroupImpl;
import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import ru.kil0bait.magnifier.classes.FourierImage;
import ru.kil0bait.magnifier.classes.FourierPair;
import ru.kil0bait.magnifier.classes.MagniImage;
import ru.kil0bait.magnifier.classes.MagniPixel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.kil0bait.magnifier.TestUtils.*;
import static ru.kil0bait.magnifier.TestUtils.saveMagniImageToFolder;

public class HdfTest {
    public static final Pattern VELOX_META_PATTERN = Pattern.compile("\"label\": \"([^\"]*)\".*\"dataPath\": \"([^\"]*)\"", Pattern.DOTALL);
    public static final String[] DET_SEQ = {"DF4-A", "DF4-B", "DF4-C", "DF4-D"};
    public static final String[] DET_SEQ2 = {"DF4-A", "DF4-D", "DF4-C", "DF4-B"};

    public static void main(String[] args) throws IOException {
//        MagniImage image = new MagniImage(fromHdf("hdf5/in.emd", "Data/Image/1013a080eb4c439eb0a5029fbe8b2bd3/Data"));
//        saveMagniImageToFolder(image.dynamicNorm(), "hdf5", "1013a080eb4c439eb0a5029fbe8b2bd3");
//        List<Dataset> datasets = allImages("hdf5/in.emd");
//        List<MagniImage> images = getAllImages("hdf5/in.emd");
//        for (MagniImage image : images) {
//            saveNormMagniImage(image, "hdf5");
//        }
//        testIdpc("hdf5", "hdf5/in.emd");
//        testIdpc2();
        List<MagniImage> magniImages = allImages("hdf5/in.emd");
        for (int i = 0; i < magniImages.size(); i++) {
            saveMagniImageToFolder(magniImages.get(i).dynamicNorm(), "hdf5_1901", magniImages.get(i).getName()+i);
        }
    }

    public static void testIdpc2() throws IOException {
        List<MagniImage> magniImages = dpcImagesFromVelox("hdf5/in.emd");
        BufferedWriter writer;
        for (MagniImage magniImage : magniImages) {
            saveMagniImageToFolder(magniImage.dynamicNorm(), "hdf5_1901", magniImage.getName());
            String s = "D:/4.documents/projects/IdeaProjects/magnifier_res/out/hdf5_1901/" + magniImage.getName() + ".txt";
            writer = new BufferedWriter(new FileWriter(s));
            writer.write(magniImage.rawValues());
            writer.close();
        }
    }

    public static void testIdpc(String folder, String hdfFilePath) {
        MagniImage[] images = detImagesFromVelox(hdfFilePath, DET_SEQ);
        FourierImage idpcImage = calculateIdpc(images);
        printFirstValues(idpcImage.getImageFromRe(), 16);
//        MagniImage idpcImage = calculateIdpcDist(images);
//        printFirstValues(idpcImage, 16);
        saveMagniImageToFolder(idpcImage.getImageFromRe().negative().dynamicNorm(), folder, "idpcCalculated");

//        FourierImage fourierImage = new FourierImage(images[0]);
//        FourierImage forward = fourierImage.fftCooleyForward();
//        FourierImage inverse = forward.fftCooleyInverse();
//        for (int y = 0; y < 16; y++) {
//            for (int x = 0; x < 16; x++) {
//                System.out.printf("%f    ", inverse.getImageFromRe().pixel(x, y).getIntensity());
//            }
//            System.out.println();
//        }
    }

    public static MagniImage[] detImagesFromVelox(String hdfFilePath, String[] detSeq) {
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
        try (HdfFile in = new HdfFile(Paths.get(buildPath("in", hdfFilePath)))) {
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

    public static List<MagniImage> allImages(String filePath) {
        List<MagniImage> res = new ArrayList<>();
        try (HdfFile in = new HdfFile(Paths.get(buildPath("in", filePath)))) {
            Group magniImages = (Group) ((Group) in.getChild("Data")).getChild("Image");
            for (Map.Entry<String, Node> entry : magniImages.getChildren().entrySet()) {
                System.out.printf("%s  %s\r\n", entry.getKey(), entry.getValue());
                String s = entry.getValue().getPath() + "Data";
                System.out.println(s);


                Map<String, Node> children = ((GroupImpl) entry.getValue()).getChildren();
                Dataset dataset = (Dataset) children.get("Data");
                MagniImage image = imageFromDataset(dataset);
                image.setName(entry.getValue().getName());
                res.add(image);

//                String[] data = new String[5];
//                String[] data = (String[]) (((GroupImpl) entry.getValue()).getChildren());
//                Matcher matcher = VELOX_META_PATTERN.matcher(data[0]);
//                if (matcher.find()) {
//                    String label = matcher.group(1);
//                    String imagePath = matcher.group(2).replaceAll("\\\\", "");
//                    Dataset dataset = in.getDatasetByPath(imagePath + "/Data");
//                }
            }
        }
        return res;
    }

    public static List<MagniImage> getAllImages(String hdfFilePath) {
        List<MagniImage> res = new ArrayList<>();
        try (HdfFile in = new HdfFile(Paths.get(buildPath("in", hdfFilePath)))) {
            Group data = (Group) ((Group) in.getChild("Data")).getChild("Image");
            for (Map.Entry<String, Node> entry : data.getChildren().entrySet()) {
                MagniImage image = imageFromDataset(in.getDatasetByPath(entry.getValue().getPath() + "Data"));
                image.setName(entry.getKey());
                res.add(image);
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

    public static MagniPixel[][] fromHdf(String filePath, String datasetPath) {
        MagniPixel[][] res = null;
        try (HdfFile in = new HdfFile(Paths.get(buildPath("in", filePath)))) {
            Dataset datasetByPath = in.getDatasetByPath(datasetPath);
            int[][][] array = (int[][][]) datasetByPath.getData();
            res = new MagniPixel[array.length][array[0].length];
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    res[i][j] = new MagniPixel(array[i][j][0]);
                }
            }
        }
        return res;
    }

    private static FourierImage calculateIdpc(MagniImage[] images) {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        FourierImage deGradient = fourierPair.deGradientDefault();
        return deGradient.fftCooleyInverse();
    }

    private static MagniImage calculateIdpcDist(MagniImage[] images) {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        FourierPair fourierPair = new FourierPair(imageX, imageY);
        fourierPair.fftCooleyForward();
        return fourierPair.deGradientDefaultDist();
    }

    private static void printFirstValues(MagniImage image, int size) {
        System.out.println("-----Start-----");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++)
                System.out.printf("%f    ", image.pixel(y, x).getIntensity());
            System.out.println();
        }
        System.out.println("-----END-----");
    }
}
