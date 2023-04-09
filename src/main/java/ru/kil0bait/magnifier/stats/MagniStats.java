package ru.kil0bait.magnifier.stats;

import ru.kil0bait.magnifier.classes.MagniImage;

public class MagniStats {
    private final MagniImage norm;
    private final int[] histo256;
    private final double max;
    private final double min;

    public MagniStats(MagniImage norm, int[] histo256, double max, double min) {
        this.norm = norm;
        this.histo256 = histo256;
        this.max = max;
        this.min = min;
    }

    public MagniImage getNorm() {
        return norm;
    }

    public int[] getHisto256() {
        return histo256;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
}
