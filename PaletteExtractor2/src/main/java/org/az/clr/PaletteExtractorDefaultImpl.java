package org.az.clr;

import static org.az.clr.ColorTools.toRGBColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.az.clr.ColorTools.RgbColor;
import org.az.collections.MapOfLists;

/**
*
* @author Artem Zaborskiy
*
*/
public class PaletteExtractorDefaultImpl implements PaletteExtractor {
    int MIN = 0;

    int MAX = 1;

    public MapOfLists<ColorTools.RgbColor, Integer> assignColorsToSeeds(
            final RgbColor[] seeds, final int[] argbImage) {
        final MapOfLists<ColorTools.RgbColor, Integer> clusters = new MapOfLists<ColorTools.RgbColor, Integer>();
        for (final int rgb : argbImage) {

            final RgbColor rgbc = toRGBColor(rgb);

            int seedIndex = 0;
            int minDistance = seeds[0].distance(rgbc);

            for (int f = 0; f < seeds.length; f++) {
                final RgbColor seed = seeds[f];
                final int distance = rgbc.distance(seed);
                if (distance < minDistance) {
                    seedIndex = f;
                    minDistance = distance;
                }
            }

            clusters.put(seeds[seedIndex], rgb);
        }

        return clusters;
    }

    public void extract(final int[] argbImage, final int paletteSize) {
        final MapOfLists<ColorTools.RgbColor, Integer> clusters = new MapOfLists<ColorTools.RgbColor, Integer>();
        final ArrayList<Integer> colors = new ArrayList<Integer>();
        for (final int rgb : argbImage) {
            colors.add(rgb);
        }

        clusters.getMap().put(toRGBColor(0x666666), colors);

        while (clusters.size() < paletteSize) {
            final RgbColor biggestClusterCode = findBiggestCluster(clusters);
            spiltCluster(biggestClusterCode, clusters);
        }

        for (final RgbColor c : clusters.keySet()) {
            System.out.print(c.toHexString() + ",");
        }

    }

    private RgbColor findBiggestCluster(
            final MapOfLists<ColorTools.RgbColor, Integer> clusters) {

        long maxVol = 0;
        RgbColor key = null;
        for (final RgbColor c : clusters.keySet()) {
            final List<Integer> colors = clusters.get(c);
            final RgbColor[] sizeOfColorBox = ColorTools.size(colors);
            final RgbColor size = size(sizeOfColorBox);
            final long volume = size.getR() * size.getG() * size.getB();
            if (volume > maxVol) {
                maxVol = volume;
                key = c;
            }
        }
        return key;
    }

    private int findLongestComponent(final RgbColor size) {

        final int r = 1 * size.getR();
        final int g = 1 * size.getG();
        final int b = 1 * size.getB();

        if (r > g && r > b) {
            return 0;
        }
        if (g > r && g > b) {
            return 1;
        }
        return 2;
    }

    private RgbColor size(final RgbColor[] sizeOfColorBox) {
        final RgbColor size = new RgbColor(
                //
                sizeOfColorBox[MAX].getR() - sizeOfColorBox[MIN].getR(),
                sizeOfColorBox[MAX].getG() - sizeOfColorBox[MIN].getG(),
                sizeOfColorBox[MAX].getB() - sizeOfColorBox[MIN].getB());
        return size;
    }

    private void spiltCluster(final RgbColor color,
            final MapOfLists<ColorTools.RgbColor, Integer> clusters) {
        final List<Integer> colors = clusters.get(color);
        final RgbColor[] sizeOfColorBox = ColorTools.size(colors);
        final RgbColor size = size(sizeOfColorBox);

        final int longestSide = findLongestComponent(size);
        // RgbColor midpoint = midPoint(sizeOfColorBox);

        // temporal keys
        final RgbColor listKey1 = new RgbColor(-1, 0, 0);
        final RgbColor listKey2 = new RgbColor(0, -1, 0);

        Collections.sort(colors, new Comparator<Integer>() {

            @Override
            public int compare(final Integer o1, final Integer o2) {
                final RgbColor c1 = toRGBColor(o1);
                final RgbColor c2 = toRGBColor(o2);
                return c1.getRgb()[longestSide] < c2.getRgb()[longestSide] ? -1
                        : 1;
            }
        });

        for (int i = 0; i < colors.size(); i++) {
            if (i < colors.size() / 2) {
                clusters.put(listKey1, colors.get(i));
            } else {
                clusters.put(listKey2, colors.get(i));
            }
        }

        // removing old cluster
        clusters.removeKey(color);

        final RgbColor newKey1 = ColorTools.meanColor(clusters.get(listKey1));
        final RgbColor newKey2 = ColorTools.meanColor(clusters.get(listKey2));

        clusters.getMap().put(newKey1, clusters.get(listKey1));
        clusters.getMap().put(newKey2, clusters.get(listKey2));

        // removing temp keys
        clusters.removeKey(listKey1);
        clusters.removeKey(listKey2);

    }

}
