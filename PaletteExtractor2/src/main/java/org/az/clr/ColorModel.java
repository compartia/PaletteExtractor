package org.az.clr;

import org.az.clr.ColorTools.HsvColor;
import org.az.clr.ColorTools.RgbColor;

/**
 *
 * @author Artem Zaborskiy
 *
 */
public interface ColorModel {
    int distance(HsvColor c1, HsvColor c2);

    int distance(RgbColor c1, RgbColor c2);

    int getHueRange();

    HsvColor toHSVColor(int rgb);

    HsvColor toHSVColor(RgbColor rgb);

    RgbColor toRGB(HsvColor hsv);
}
