package org.az.clr;

import static org.az.clr.ColorTools.toRGBColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.az.clr.ColorTools.HsvColor;
import org.az.clr.ColorTools.RgbColor;
import org.az.collections.MapOfLists;

public class PaletteExtractorNative2 implements PaletteExtractor {
    private final ColorModel cm;

    final static RgbColor[] defaultSeeds = {
            //
            toRGBColor(0xff0000),
            toRGBColor(0x00ff00),
            toRGBColor(0x0000ff),
            //
            toRGBColor(0x00ffff),
            toRGBColor(0xffff00),
            toRGBColor(0xff00ff),
            //
            toRGBColor(0x666666),
            toRGBColor(0x660000),
            toRGBColor(0x006600),
            toRGBColor(0x000066),
            toRGBColor(0x006666),
            toRGBColor(0x666600),
            toRGBColor(0x660066),
            //
            toRGBColor(0x000000),
            toRGBColor(0xffffff) };

    public PaletteExtractorNative2(final ColorModel cm) {
        this.cm = cm;
    }

    public MapOfLists<ColorTools.RgbColor, Integer> assignColorsToSeeds(
            final Set<RgbColor> seedsRGB, final List<Integer> argbImage) {
        final MapOfLists<ColorTools.RgbColor, Integer> clusters = new MapOfLists<ColorTools.RgbColor, Integer>();

        final Set<HsvColor> seedsHSV = toHSV(seedsRGB);

        for (final int rgb : argbImage) {

            final HsvColor rgbc = cm.toHSVColor(rgb);

            HsvColor bestSeed = seedsHSV.iterator().next();
            int minDistance = cm.distance(bestSeed, rgbc);

            for (final HsvColor seed : seedsHSV) {

                final int distance = cm.distance(seed, rgbc); // seed.distance(rgbc);
                if (distance < minDistance) {
                    bestSeed = seed;
                    minDistance = distance;
                }
            }

            clusters.put(cm.toRGB(bestSeed), rgb);
        }

        return clusters;
    }

    public RgbColor[] extract(final int[] argbImage, final int paletteSize) {

        Set<RgbColor> seeds = makeSeeds(paletteSize);

        final List<Integer> colors = new ArrayList<Integer>(argbImage.length);
        for (final int i : argbImage) {
            colors.add(Integer.valueOf(i));
        }

        // 1) for each point find nearest seed
        // 2) move seeds to center of these groups
        // 3) remove weak groups
        MapOfLists<RgbColor, Integer> clusters = null;
        for (int f = 0; f < 4; f++) {
            clusters = assignColorsToSeeds(seeds, colors);
            seeds = moveSeeds(clusters);
        }

        clusters = assignColorsToSeeds(seeds, colors);
        seeds = moveSeedsToBest(clusters);

        final RgbColor[] ret = new RgbColor[seeds.size()];
        seeds.toArray(ret); ///XXX SORT IT
        return ret;
    }

    private Set<RgbColor> makeSeeds(final int number) {
        final int extra = number;
        final Set<RgbColor> seeds = new HashSet<ColorTools.RgbColor>();
        for (int f = 0; f < extra; f++) {
            seeds.add(defaultSeeds[f]);
        }

        return seeds;
    }

    private Set<RgbColor> moveSeeds(final MapOfLists<RgbColor, Integer> clusters) {
        final List<Entry<RgbColor, List<Integer>>> sorted = clusters
                .sortByListSize(false);

        final Set<RgbColor> seeds = new HashSet<ColorTools.RgbColor>();

        for (int i = 0; i < sorted.size(); i++) {
            final Entry<RgbColor, List<Integer>> e = sorted.get(i);
            final List<Integer> list = e.getValue();

            final RgbColor mean = ColorTools.meanColor(list);

            seeds.add(mean);
        }
        return seeds;
    }

    private Set<RgbColor> moveSeedsToBest(final MapOfLists<RgbColor, Integer> clusters) {
        final List<Entry<RgbColor, List<Integer>>> sorted = clusters
                .sortByListSize(false);

        final Set<RgbColor> seeds = new HashSet<ColorTools.RgbColor>();

        for (int i = 0; i < sorted.size(); i++) {
            final Entry<RgbColor, List<Integer>> e = sorted.get(i);
            final List<Integer> list = e.getValue();

            final RgbColor mean = ColorTools.bestColor(list, cm);

            seeds.add(mean);
        }
        return seeds;
    }

    private Set<HsvColor> toHSV(final Set<RgbColor> seedsRGB) {
        final Set<HsvColor> seedsHSV = new HashSet<ColorTools.HsvColor>();
        for (final RgbColor seed : seedsRGB) {
            seedsHSV.add(cm.toHSVColor(seed));
        }
        return seedsHSV;
    }

}
