package org.az.clr;

import java.util.List;

/**
 *
 * @author Artem Zaborskiy
 *
 */
public class ColorTools {

    public static class HsvColor {

        private int[] hsv = { 0, 0, 0 };

        public HsvColor() {
        }

        public HsvColor(final float[] hsv) {
            super();
            this.hsv[0] = ((int)hsv[0]);
            this.hsv[1] = ((int)(hsv[1] * 255f));
            this.hsv[2] = ((int)(hsv[2] * 255f));
        }

        public HsvColor(final int h, final int s, final int v) {
            super();
            this.setH(h);
            this.setS(s);
            this.setV(v);
        }

        public int[] getHsv() {
            return hsv;
        }

        public int getS() {
            return hsv[1];
        }

        public void setHsv(final int[] hsv) {
            this.hsv = hsv;
        }

        public void setS(final int s) {
            hsv[1] = s;
        }

        @Override
        public String toString() {
            return "HsvColor [h=" + getH() + ", s=" + getS() + ", v=" + getV()
                    + "]";
        }

        int getH() {
            return hsv[0];
        }

        int getV() {
            return hsv[2];
        }

        void setH(final int h) {
            hsv[0] = h;
        }

        void setV(final int v) {
            hsv[2] = v;
        }

    }

    public static class RgbColor {
        private int[] rgb = { 0, 0, 0 };

        public RgbColor() {
        }

        public RgbColor(final int r, final int g, final int b) {
            super();
            this.setR(r);
            this.setG(g);
            this.setB(b);
        }

        /**
         * 0.3 R + 0.6 G + 0.1 B;
         *
         * @deprecated Use colorModel interface
         * @return
         */
        @Deprecated
        public int distance(final RgbColor c) {
            final HsvColor c1 = rgbToHsv(this);
            final HsvColor c2 = rgbToHsv(c);
            // int hueDiff = c1.h - c2.h ;

            final int d = Math.abs(c1.getH() - c2.getH()) % 256;
            final int hueDiff = d > 127 ? 256 - d : d;

            return hueDiff * hueDiff + (c1.getS() - c2.getS())
                    * (c1.getS() - c2.getS()) + (c1.getV() - c2.getV())
                    * (c1.getV() - c2.getV());// + //
            // (c.getG() - getG()) * (c.getG() - getG());// +
            // distanceRgb(c)
            // / 4;
        }

        public int distanceR(final int rgb) {

            final int rDif = ((rgb >> 16) & 0xFF) - getR();
            final int gDif = ((rgb >> 8) & 0xFF) - getG();
            final int bDif = (rgb & 0xFF) - getB();

            return 2 * rDif * rDif + 4 * gDif * gDif + bDif * bDif;
        }

        public int distanceRgb(final RgbColor c2) {

            // HsvColor h1 = rgbToHsv(this);
            // HsvColor h2 = rgbToHsv(c2);

            final int rDif = (c2.getR() - getR());
            final int gDif = (c2.getG() - getG());
            final int bDif = c2.getB() - getB();

            return rDif * rDif + gDif * gDif + bDif * bDif;
        }

        public int getB() {
            return rgb[2];
        }

        public int getG() {
            return rgb[1];
        }

        public int getR() {
            return rgb[0];
        }

        public int[] getRgb() {
            return rgb;
        }

        public void setB(final int b) {
            rgb[2] = b;
        }

        public void setG(final int g) {
            rgb[1] = g;
        }

        public void setR(final int r) {
            rgb[0] = r;
        }

        public void setRgb(final int[] rgb) {
            this.rgb = rgb;
        }

        public String toHexString() {

            return String.format("%02X", (0xFF & getR()))
                    + String.format("%02X", (0xFF & getG()))
                    + String.format("%02X", (0xFF & getB()));
        }

        public int toRGBInt() {
            final int ret = getR() << 16 | getG() << 8 | getB();
            return ret;
        }

        @Override
        public String toString() {
            return "RgbColor [r=" + getR() + ", g=" + getG() + ", b=" + getB()
                    + "]" + toHexString();
        }

    }

    public static RgbColor bestColor(final List<Integer> colors, final ColorModel cm) {
        final RgbColor mean = meanColor(colors);
        final HsvColor meanHsv = cm.toHSVColor(mean);
        long distanceSumm = 0;
        for (final Integer i : colors) {
            final int distance = cm.distance(cm.toHSVColor(i), meanHsv);
            distanceSumm += distance;
        }
        final int meanDistance = (int)(distanceSumm / colors.size());

        // /
        int cnt = 0;
        long r = 0;
        long g = 0;
        long b = 0;
        for (final Integer i : colors) {
            final RgbColor c = toRGBColor(i);
            final int distance = cm.distance(cm.toHSVColor(i), meanHsv);
            if (distance <= meanDistance) {
                cnt++;
                r += c.getR();
                g += c.getG();
                b += c.getB();
            }
        }

        r = (r / cnt);
        g = (g / cnt);
        b = (b / cnt);
        return new RgbColor((int)r, (int)g, (int)b);
    }

    public static RgbColor blend(final float blend, final RgbColor c1, final RgbColor c2) {
        final float r = blend * c1.getR() + (1 - blend) * c2.getR();
        final float g = blend * c1.getG() + (1 - blend) * c2.getG();
        final float b = blend * c1.getB() + (1 - blend) * c2.getB();

        return new RgbColor((int)r, (int)g, (int)b);
    }

    public static RgbColor hsvToRgb(final HsvColor hsv) {
        final RgbColor rgb = new RgbColor();
        int region, remainder, p, q, t;

        if (hsv.getS() == 0) {
            rgb.setR(hsv.getV());
            rgb.setG(hsv.getV());
            rgb.setB(hsv.getV());
            return rgb;
        }

        region = hsv.getH() / 43;
        remainder = (hsv.getH() - (region * 43)) * 6;

        p = (hsv.getV() * (255 - hsv.getS())) >> 8;
        q = (hsv.getV() * (255 - ((hsv.getS() * remainder) >> 8))) >> 8;
        t = (hsv.getV() * (255 - ((hsv.getS() * (255 - remainder)) >> 8))) >> 8;

        switch (region) {
            case 0:
                rgb.setR(hsv.getV());
                rgb.setG(t);
                rgb.setB(p);
                break;
            case 1:
                rgb.setR(q);
                rgb.setG(hsv.getV());
                rgb.setB(p);
                break;
            case 2:
                rgb.setR(p);
                rgb.setG(hsv.getV());
                rgb.setB(t);
                break;
            case 3:
                rgb.setR(p);
                rgb.setG(q);
                rgb.setB(hsv.getV());
                break;
            case 4:
                rgb.setR(t);
                rgb.setG(p);
                rgb.setB(hsv.getV());
                break;
            default:
                rgb.setR(hsv.getV());
                rgb.setG(p);
                rgb.setB(q);
                break;
        }

        return rgb;
    }

    // public static RgbColor medianColor(List<Integer> colors) {
    // for (Integer i : colors) {
    // RgbColor c = toRGBColor(i);
    //
    // }
    // }
    public static RgbColor meanColor(final List<Integer> colors) {
        long r = 0;
        long g = 0;
        long b = 0;
        for (final Integer i : colors) {
            final RgbColor c = toRGBColor(i);
            r += c.getR();
            g += c.getG();
            b += c.getB();
        }

        r = (r / colors.size());
        g = (g / colors.size());
        b = (b / colors.size());
        return new RgbColor((int)r, (int)g, (int)b);
    }

    public static RgbColor meanHsvColor(final List<Integer> colors, final ColorModel cm) {
        double h1 = 0;
        double h2 = 0;
        long s = 0;
        long v = 0;
        for (final Integer i : colors) {
            final HsvColor c = cm.toHSVColor(i);

            h1 += Math.sin(Math.toRadians(c.getH()));
            h2 += Math.cos(Math.toRadians(c.getH()));
            s += c.getS();
            v += c.getV();
        }

        h1 = (h1 / colors.size());
        h2 = (h2 / colors.size());
        final double h = Math.toDegrees(Math.atan2(h1, h2));

        s = (s / colors.size());
        v = (v / colors.size());
        final HsvColor hsvColor = new HsvColor((int)h, (int)s, (int)v);
        return cm.toRGB(hsvColor);
    }

    /**
     * 0.3 R + 0.6 G + 0.1 B;
     *
     * @deprecated Use colorModel interface
     * @return
     */
    @Deprecated
    public static HsvColor rgbToHsv(final RgbColor rgb) {
        final HsvColor hsv = new HsvColor();
        int rgbMin, rgbMax;

        rgbMin = rgb.getR() < rgb.getG() ? (rgb.getR() < rgb.getB() ? rgb
                .getR() : rgb.getB()) : (rgb.getG() < rgb.getB() ? rgb.getG()
                : rgb.getB());
        rgbMax = rgb.getR() > rgb.getG() ? (rgb.getR() > rgb.getB() ? rgb
                .getR() : rgb.getB()) : (rgb.getG() > rgb.getB() ? rgb.getG()
                : rgb.getB());

        hsv.setV(rgbMax);
        if (hsv.getV() == 0) {
            hsv.setH(0);
            hsv.setS(0);
            return hsv;
        }

        hsv.setS(255 * (rgbMax - rgbMin) / hsv.getV());
        if (hsv.getS() == 0) {
            hsv.setH(0);
            return hsv;
        }

        if (rgbMax == rgb.getR()) {
            hsv.setH(0 + 43 * (rgb.getG() - rgb.getB()) / (rgbMax - rgbMin));
        } else if (rgbMax == rgb.getG()) {
            hsv.setH(85 + 43 * (rgb.getB() - rgb.getR()) / (rgbMax - rgbMin));
        } else {
            hsv.setH(171 + 43 * (rgb.getR() - rgb.getG()) / (rgbMax - rgbMin));
        }

        if (hsv.getH() < 0) {
            hsv.setH(hsv.getH() + 360);
        }

        return hsv;
    }

    public static RgbColor[] size(final List<Integer> colors) {
        final RgbColor[] ret = { toRGBColor(0xffffff), toRGBColor(0x000000) };
        for (final Integer i : colors) {
            final RgbColor c = toRGBColor(i);
            ret[0].setR(Math.min(ret[0].getR(), c.getR()));
            ret[0].setG(Math.min(ret[0].getG(), c.getG()));
            ret[0].setB(Math.min(ret[0].getB(), c.getB()));

            ret[1].setR(Math.max(ret[1].getR(), c.getR()));
            ret[1].setG(Math.max(ret[1].getG(), c.getG()));
            ret[1].setB(Math.max(ret[1].getB(), c.getB()));
        }
        return ret;
    }

    public static RgbColor[] sizeOfColorBox(final List<RgbColor> colors) {
        final RgbColor[] ret = { toRGBColor(0xffffff), toRGBColor(0x000000) };
        for (final RgbColor c : colors) {

            ret[0].setR(Math.min(ret[0].getR(), c.getR()));
            ret[0].setG(Math.min(ret[0].getG(), c.getG()));
            ret[0].setB(Math.min(ret[0].getB(), c.getB()));

            ret[1].setR(Math.max(ret[1].getR(), c.getR()));
            ret[1].setG(Math.max(ret[1].getG(), c.getG()));
            ret[1].setB(Math.max(ret[1].getB(), c.getB()));
        }
        return ret;
    }

    public static RgbColor toRGBColor(final int rgb) {
        final RgbColor rgbc = new RgbColor();

        rgbc.setR((rgb >> 16) & 0xFF);
        rgbc.setG((rgb >> 8) & 0xFF);
        rgbc.setB(rgb & 0xFF);
        return rgbc;
    }

    public static int toRGBInt(final int r, final int g, final int b) {
        final int ret = r << 16 | g << 8 | b;
        return ret;
    }

}
