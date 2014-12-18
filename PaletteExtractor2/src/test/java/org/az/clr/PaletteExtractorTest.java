package org.az.clr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.az.clr.ColorTools.RgbColor;
import org.junit.Test;

public class PaletteExtractorTest {

    public static void extractPal(final String name) throws IOException {
        final int[] bytes = loadImage(PaletteExtractorTest.class.getClassLoader().getResourceAsStream(name));
        final PaletteExtractorNative pe = new PaletteExtractorNative(new DefaultColorModel());
        final RgbColor[] seeds = pe.extract(bytes, 6);

        printPal(seeds, name);
    }

    public static int[] loadImage(final InputStream ss) throws IOException {
        // open image
        // File imgPath = new File(ImageName);
        final BufferedImage bi = ImageIO.read(ss);

        final BufferedImage off_Image = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2 = off_Image.createGraphics();
        g2.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), null);

        // get DataBufferBytes from Raster
        final WritableRaster raster = off_Image.getRaster();
        final DataBufferInt data = (DataBufferInt)raster.getDataBuffer();

        return (data.getData());
    }

    public static void printPal(final RgbColor[] seeds, final String name) {
        System.out.println();
        System.out.println("seeds palette: " + name + " ");
        for (final RgbColor seed : seeds) {
            System.out.print(seed.toHexString() + ",");
        }
        System.out.println();
    }

    public int dh(final DefaultColorModel dm, final int c1, final int c2) {
        final int[] hue1 = dm.toHSVColor(c1).getHsv();
        final int[] hue2 = dm.toHSVColor(c2).getHsv();

        System.out.println("HU1=" + hue1[0] + " HUE2=" + hue2[0]);
        final int distanceHue = dm.distanceHue(hue1, hue2);

        return distanceHue;
    }

    @Test
    public void testDistanceHsv() throws IOException {
        final DefaultColorModel dm = new DefaultColorModel();

        System.out.println("HSV red-black D=" + dh(dm, 0xff0000, 0));
        System.out.println("HSV red-pink D=" + dh(dm, 0xff6666, 0xff0000));
        System.out.println("HSV orange-violet D=" + dh(dm, 0xFF1100, 0xFF0011));
    }

    @Test
    public void testHsv() throws IOException {
        final ColorTools.RgbColor rgb = new ColorTools.RgbColor();
        rgb.setR(0);
        rgb.setB(0);
        rgb.setG(0);
        System.out.println("HSV of black = " + ColorTools.rgbToHsv(rgb));

        rgb.setR(30);
        rgb.setB(30);
        rgb.setG(30);
        System.out.println("HSV of grey = " + ColorTools.rgbToHsv(rgb));

        rgb.setR(255);
        rgb.setB(0);
        rgb.setG(0);
        System.out.println("HSV of red = " + ColorTools.rgbToHsv(rgb));

        rgb.setR(0);
        rgb.setB(0);
        rgb.setG(1);
        System.out.println("HSV of darker green  = " + ColorTools.rgbToHsv(rgb));
    }

    @Test
    public void testPalettes() throws IOException {

        {
            final String name = "paletteBW.png";
            final int[] bytes = loadImage(getClass().getClassLoader()
                    .getResourceAsStream(name));
            final PaletteExtractorNative pe = new PaletteExtractorNative(new DefaultColorModel());
            final RgbColor[] seeds = pe.extract(bytes, 6);

            printPal(seeds, name);
        }

    }

    @Test
    public void testPalettes1() throws IOException {
        final String name = "paletteBWG.png";
        extractPal(name);
    }

    @Test
    public void testPalettes2() throws IOException {
        final String name = "paletteBWGR.png";
        extractPal(name);
    }

    @Test
    public void testPalettesG() throws IOException {
        final String name = "paletteG.png";
        extractPal(name);
    }

    @Test
    public void testPalettesRepun() throws IOException {
        final String name = "Repin_saporoher_kosaken (1).jpg";
        extractPal(name);
    }

    @Test
    public void testRgb() {
        final RgbColor c = new RgbColor(0x35, 0x36, 0x37);
        final int rgbInt = c.toRGBInt();
        System.out.println(c.toHexString() + " = " + ColorTools.toRGBColor(rgbInt));
    }

}
