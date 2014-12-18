package org.az.clr;

import static org.az.clr.ColorTools.toRGBColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.az.clr.ColorTools.RgbColor;
import org.az.collections.MapOfLists;

public class PaletteExtractor_KMeans {

    final static RgbColor[] defaultSeeds = { toRGBColor(0x000000),
            toRGBColor(0xffffff), toRGBColor(0xff0000), toRGBColor(0x00ff00),
            toRGBColor(0x0000ff), toRGBColor(0x00ffff),
            toRGBColor(0xffff00),
            toRGBColor(0xff00ff),
            //
            toRGBColor(0x666666),
            toRGBColor(0x660000),
            toRGBColor(0x006600),
            toRGBColor(0x000066),
            toRGBColor(0x006666),
            toRGBColor(0x666600),
            toRGBColor(0x660066) };

    public MapOfLists<ColorTools.RgbColor, Integer> assignColorsToSeeds(
            final RgbColor[] seeds, final List<Integer> argbImage) {
        final MapOfLists<ColorTools.RgbColor, Integer> clusters = new MapOfLists<ColorTools.RgbColor, Integer>();
        for (final int rgb : argbImage) {

            final RgbColor rgbc = toRGBColor(rgb);

            int seedIndex = 0;
            int minDistance = seeds[0].distance(rgbc);

            for (int f = 0; f < seeds.length; f++) {
                final RgbColor seed = seeds[f];
                final int distance = seed.distance(rgbc);
                if (distance < minDistance) {
                    seedIndex = f;
                    minDistance = distance;
                }
            }

            clusters.put(seeds[seedIndex], rgb);
        }
        return clusters;
    }

    public RgbColor[] extract(final int[] argbImage, final int paletteSize) {
        RgbColor[] seeds = makeSeeds(paletteSize);

        final List<Integer> colors = new ArrayList<Integer>(argbImage.length);
        for (final int i : argbImage) {
            colors.add(Integer.valueOf(i));
        }

        // 1) for each point find nearest seed
        // 2) move seeds to center of these groups
        // 3) remove weak groups
        MapOfLists<RgbColor, Integer> clusters = null;
        for (int f = 0; f < 7; f++) {
            clusters = assignColorsToSeeds(seeds, colors);
            seeds = moveSeeds(clusters);
        }

        return seeds;
    }

    private RgbColor clusterCenterColor(final List<Integer> colors) {
        RgbColor[] seeds = makeSeeds(2);
        for (int f = 0; f < 6; f++) {
            final MapOfLists<RgbColor, Integer> clusters = assignColorsToSeeds(seeds,
                    colors);
            seeds = moveSeeds(clusters);
        }

        return seeds[0];
    }

    /**
     * 0.3 R + 0.6 G + 0.1 B;
     *
     * @return
     */
    private RgbColor[] make256Seeds() {
        final RgbColor[] seeds = new RgbColor[256];
        int index = 0;
        final int rINc = 32;
        final int gINc = 32;
        final int bINc = 64;

        for (int r = rINc / 2; r < 256; r += rINc) {
            for (int g = gINc / 2; g < 256; g += gINc) {
                for (int b = bINc / 2; b < 256; b += bINc) {
                    seeds[index] = new RgbColor(r, g, b);
                    System.out.println(seeds[index].toString());
                    index++;
                }
            }
        }

        return seeds;
    }

    private RgbColor[] makeSeeds(final int number) {
        final int extra = number;
        final RgbColor[] seeds = new RgbColor[extra];

        for (int f = 0; f < extra; f++) {
            seeds[f] = defaultSeeds[f];
        }

        return seeds;
    }

    private RgbColor[] moveSeeds(final MapOfLists<RgbColor, Integer> clusters) {
        final List<Entry<RgbColor, List<Integer>>> sorted = clusters
                .sortByListSize(false);

        final RgbColor[] seeds = new RgbColor[sorted.size()];

        for (int i = 0; i < seeds.length; i++) {
            final Entry<RgbColor, List<Integer>> e = sorted.get(i);
            final List<Integer> list = e.getValue();

            final RgbColor mean = ColorTools.meanColor(list);

            seeds[i] = mean;
        }
        return seeds;
    }

}
