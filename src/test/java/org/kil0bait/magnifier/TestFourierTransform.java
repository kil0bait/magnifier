package org.kil0bait.magnifier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kil0bait.magnifier.base.FourierImage;
import org.kil0bait.magnifier.base.MagniImage;
import org.kil0bait.magnifier.vector.ComplexVectorImage;

public class TestFourierTransform {
    static String folder = "aprilFourier";
    static MagniImage image1;
    static MagniImage image2;

    @BeforeAll
    public static void setupClass() {
        image1 = new MagniImage(TestUtils.loadImageFromOut(folder + "/123.png"));
        image2 = new MagniImage(TestUtils.loadImageFromOut(folder + "/123a.png"));
    }

    @Test
    public void testDftFourierImage() {
        FourierImage fourierImage = new FourierImage(image1);
        FourierImage dftSlowForward = fourierImage.dftSlowForward();
        TestUtils.saveMagniImageWithName(dftSlowForward.spectrumFourier().dynamicNorm(), folder, "spectrum");
        TestUtils.saveMagniImageWithName(dftSlowForward.spectrumFourier().dynamicNormAverage(), folder, "spectrum_be");
        FourierImage dftSlowInverse = dftSlowForward.dftSlowInverse();
        TestUtils.saveMagniImageWithName(dftSlowInverse.imageFromRe().dynamicNorm(), folder, "after");
    }

    @Test
    public void testDftVectorImage() {
        ComplexVectorImage cvImage = new ComplexVectorImage(image1, image2);
        ComplexVectorImage dftSlowForward = cvImage.dftSlowForward();
        TestUtils.saveMagniImageWithName(dftSlowForward.spectrumFourier()[0].dynamicNormAverage(), folder, "spectrum_im1");
        FourierImage deGradient = cvImage.integratedCombine();
        FourierImage res = deGradient.dftSlowInverse();
        TestUtils.saveMagniImageWithName(res.imageFromRe().dynamicNorm(), folder, "after2");
    }
}
