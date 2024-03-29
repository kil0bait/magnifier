package org.kil0bait.magnifier.base;

import java.awt.*;

public class MagniPixel {
    private final double intensity;

    public MagniPixel(double intensity) {
        this.intensity = intensity;
    }

    public MagniPixel(MagniPixel that) {
        this.intensity = that.intensity;
    }

    public MagniPixel sumIntensity(MagniPixel that) {
        return new MagniPixel(this.intensity + that.intensity);
    }

    public MagniPixel subtractIntensity(MagniPixel that) {
        return new MagniPixel(this.intensity - that.intensity);
    }

    public MagniPixel mulByConstant(double multiplier) {
        return new MagniPixel(this.intensity * multiplier);
    }

    public static MagniPixel fromRGBColor(int rgbColor) {
        int r = (rgbColor >> 16) & 0xFF;
        int g = (rgbColor >> 8) & 0xFF;
        int b = (rgbColor & 0xFF);
        return new MagniPixel((r + g + b) / 3.0F);
    }

    public static MagniPixel fromGrayColor(int grayColor) {
        int intensity = (grayColor & 0xFF);
        return new MagniPixel(intensity);
    }

    public int getRGBInt() {
        int t = (int) Math.round(Math.max(0, Math.min(intensity, 255.0F)));
        return new Color(t, t, t).getRGB();
    }

    public double getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return String.format("intensity = %f", intensity);
    }
}
