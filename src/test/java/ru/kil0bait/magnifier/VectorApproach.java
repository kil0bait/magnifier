package ru.kil0bait.magnifier;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import ru.kil0bait.magnifier.vector.ComplexVectorImage;
import ru.kil0bait.magnifier.base.FourierImage;
import ru.kil0bait.magnifier.base.MagniImage;
import ru.kil0bait.magnifier.base.MagniPixel;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.kil0bait.magnifier.TestUtils.*;

public class VectorApproach {
    public static final Pattern VELOX_META_PATTERN = Pattern.compile("\"label\": \"([^\"]*)\".*\"dataPath\": \"([^\"]*)\"", Pattern.DOTALL);
    public static final String[] DET_SEQ = {"DF4-A", "DF4-B", "DF4-C", "DF4-D"};
    public static final String[] DET_SEQ2 = {"DF4-A", "DF4-D", "DF4-C", "DF4-B"};

    public static void main(String[] args) throws IOException {
        String folder = "aprilStage";
//        MagniImage[] images = getImagesFromPath(folder, DET_SEQ2);
        MagniImage[] images = getImagesFromVelox(folder + "/in.emd", DET_SEQ);
        idpc(images, folder);
    }


    private static void idpc(MagniImage[] images, String folder) throws IOException {
        MagniImage imageX = images[0].subtract(images[2]);
        MagniImage imageY = images[1].subtract(images[3]);
        saveMagniImageWithName(imageX.dynamicNorm(), folder, "imageX");
        saveMagniImageWithName(imageY.dynamicNorm(), folder, "imageY");

        ComplexVectorImage function = new ComplexVectorImage(imageY, imageX);
        ComplexVectorImage fftForward = function.fftCooleyForward();
//        ComplexVectorImage fftForward = function.dftSlowForward();
//        fftForward.getImage2().saveToFile(new File("out/" + folder + "/fft_imageX_vector.txt"));
        MagniImage[] magnitudes = fftForward.spectrumFourier();

        saveMagniImageWithName(magnitudes[0].dynamicNorm(), folder, "magnitude1");
        saveMagniImageWithName(magnitudes[1].dynamicNorm(), folder, "magnitude2");

//        FourierImage deGradient = fftForward.deGradient();
        FourierImage deGradient = fftForward.deGradient();
//        deGradient.shift();
        FourierImage fourierImage = deGradient
                .shiftApril()
                .fftCooleyInverse();
//                .dftSlowInverseWithCenter();

        MagniImage idpcRe = fourierImage.imageFromRe().dynamicNorm();
        saveMagniImageWithName(idpcRe, folder, "idpc_RE");
        MagniImage idpcIm = fourierImage.imageFromIm().dynamicNorm();
        saveMagniImageWithName(idpcIm, folder, "idpc_ZIM");
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
                new MagniImage(loadImageFromOut(buildPath(folder, detSeq[0] + ext))),
                new MagniImage(loadImageFromOut(buildPath(folder, detSeq[1] + ext))),
                new MagniImage(loadImageFromOut(buildPath(folder, detSeq[2] + ext))),
                new MagniImage(loadImageFromOut(buildPath(folder, detSeq[3] + ext)))
        };
    }
}
