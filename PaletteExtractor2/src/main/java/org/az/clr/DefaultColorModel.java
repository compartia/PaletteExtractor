package org.az.clr;

import org.az.clr.ColorTools.HsvColor;
import org.az.clr.ColorTools.RgbColor;

/**
 *
 * @author Artem Zaborskiy
 *
 */
public class DefaultColorModel implements ColorModel {
    public final static int HUE = 0;

    public final static int SAT = 1;

    public final static int VAL = 2;

    @Override
    public int distance(final HsvColor c1, final HsvColor c2) {
        final int[] a = c1.getHsv();
        final int[] b = c2.getHsv();

        final int hueDiff = distanceHue(a, b);

        return hueDiff * hueDiff +
                (a[SAT] - b[SAT])
                * (a[SAT] - b[SAT])
                +
                (a[VAL] - b[VAL])
                * (a[VAL] - b[VAL]);
    }

    @Override
    public int distance(final RgbColor rc1, final RgbColor rc2) {
        final HsvColor c1 = toHSVColor(rc1);
        final HsvColor c2 = toHSVColor(rc2);
        return distance(c1, c2);
    }

    public int distanceHue(final int[] a, final int[] b) {
        final int hueRange = getHueRange();
        final int d = Math.abs(a[HUE] - b[HUE]);
        int hueDiff = (d > hueRange / 2) ? (hueRange - d) : d;
        hueDiff *= 2;
        // XXX: HUE0 is Black, grey or red?

        if (a[SAT] < 10 || b[SAT] < 10 || a[VAL] < 10 || b[VAL] < 10) {
            hueDiff = a[VAL] - b[VAL];
            // probably HUE is 0, but 0 is red, which is wrong!!
        }
        return hueDiff;
    }

    @Override
    public int getHueRange() {
        return 360;
    }

    @Override
    public HsvColor toHSVColor(final int rgb) {
        return ColorTools.rgbToHsv(ColorTools.toRGBColor(rgb));
    }

    @Override
    public HsvColor toHSVColor(final RgbColor rgb) {
        return ColorTools.rgbToHsv(rgb);
    }

    @Override
    public RgbColor toRGB(final HsvColor hsv) {
        return ColorTools.hsvToRgb(hsv);
    }
}
