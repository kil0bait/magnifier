package org.kil0bait.magnifier;

import org.kil0bait.magnifier.base.MagniException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestUtils {

    public static String buildPath(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings)
            builder.append(s).append("/");
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static BufferedImage loadImageFromResources(String name) {
        try {
            return ImageIO.read(new File(TestUtils.class.getResource(name).getFile()));
        } catch (IOException e) {
            throw new MagniException("Exception while reading file");
        }
    }
}
