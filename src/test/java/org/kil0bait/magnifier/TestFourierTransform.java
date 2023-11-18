package org.kil0bait.magnifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kil0bait.magnifier.base.FourierImage;
import org.kil0bait.magnifier.base.MagniImage;

public class TestFourierTransform {
    static MagniImage smallImage;

    @BeforeAll
    public static void setupClass() {
        smallImage = new MagniImage(TestUtils.loadImageFromResources("small.png"));
    }

    @Test
    void test_dftSlowForward_success1() {
        FourierImage fourierImage = new FourierImage(smallImage);
        FourierImage dftSlowForward = fourierImage.dftSlowForward();

        Assertions.assertNotNull(dftSlowForward);
    }

}
