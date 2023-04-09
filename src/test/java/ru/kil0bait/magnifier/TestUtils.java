package ru.kil0bait.magnifier;

import ru.kil0bait.magnifier.classes.MagniImage;
import ru.kil0bait.magnifier.classes.MagniException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class TestUtils {
    public static final String IN_PATH = "in";
    public static final String OUT_PATH = "out";
    public static final String PNG_EXT = ".png";

    public static String saveBImageAsPng(BufferedImage image, String name) {
        System.out.println("Saving image " + name + ".png");
        String path = buildPath(OUT_PATH, name + PNG_EXT);
        try {
            ImageIO.write(image, "png", new File(path));
        } catch (IOException e) {
            throw new MagniException("error saving image");
        }
        return path;
    }

    public static String saveMagniImageToFolder(MagniImage image, String folder, String name) {
        System.out.println("Saving image " + folder + "/" + name + ".png");
        if (new File(buildPath(OUT_PATH, folder)).mkdir())
            System.out.println("Folder " + folder + " has been created");
        String path = buildPath(OUT_PATH, folder, name + PNG_EXT);
        try {
            ImageIO.write(image.toBufferedImage(), "png", new File(path));
        } catch (IOException e) {
            throw new MagniException("error saving image");
        }
        return path;
    }

    public static String saveNormMagniImage(MagniImage image, String folder) {
        String name = image.getName() != null ? image.getName() : String.valueOf(new Date().getTime());
        return saveMagniImageToFolder(image.dynamicNorm(), folder, name);
    }

    public static String buildPath(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings)
            builder.append(s).append("/");
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static BufferedImage loadPngImageFromIn(String name) {
        System.out.println("Load image " + name + ".png");
        try {
            return ImageIO.read(new File(buildPath(IN_PATH, name + PNG_EXT)));
        } catch (IOException e) {
            throw new MagniException("Exception while reading file");
        }
    }

    public static BufferedImage loadPngImageFromOut(String name) {
        System.out.println("Load image " + name + ".png");
        try {
            return ImageIO.read(new File(buildPath(OUT_PATH, name + PNG_EXT)));
        } catch (IOException e) {
            throw new MagniException("Exception while reading file");
        }
    }

    public static BufferedImage loadImageFromIn(String name) {
        System.out.println("Load image " + name);
        try {
            return ImageIO.read(new File(buildPath(IN_PATH, name)));
        } catch (IOException e) {
            throw new MagniException("Exception while reading file");
        }
    }

    public static BufferedImage loadImageFromOut(String name) {
        System.out.println("Load image " + name);
        try {
            return ImageIO.read(new File(buildPath(OUT_PATH, name)));
        } catch (IOException e) {
            throw new MagniException("Exception while reading file");
        }
    }

    public static void createFolderIn(String path) {
        new File(buildPath(IN_PATH, path)).mkdirs();
    }
}
