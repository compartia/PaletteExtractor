package org.az.clr;

import static org.az.clr.ColorTools.toRGBColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.az.clr.ColorTools.HsvColor;
import org.az.clr.ColorTools.RgbColor;
import org.az.collections.MapOfLists;

public class PaletteExtractorNative implements PaletteExtractor {
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

    public PaletteExtractorNative(final ColorModel cm) {
        this.cm = cm;
    }

    public MapOfLists<ColorTools.RgbColor, Integer> assignColorsToSeeds(
            final RgbColor[] seedsRGB, final List<Integer> argbImage, final boolean fillEmpySeeds) {
        final MapOfLists<ColorTools.RgbColor, Integer> clusters = new MapOfLists<ColorTools.RgbColor, Integer>();

        final HsvColor[] seedsHSV = toHSV(seedsRGB);
        final boolean[] takenSeeds = new boolean[seedsHSV.length];

        for (final int rgb : argbImage) {

            final HsvColor rgbc = cm.toHSVColor(rgb);

            int seedIndex = 0;
            final HsvColor firstSeed = seedsHSV[0];
            int minDistance = cm.distance(firstSeed, rgbc);

            for (int f = 0; f < seedsHSV.length; f++) {
                final HsvColor seed = seedsHSV[f];
                final int distance = cm.distance(seed, rgbc); // seed.distance(rgbc);
                if (distance < minDistance) {
                    seedIndex = f;
                    minDistance = distance;
                }
            }

            takenSeeds[seedIndex] = true;
            clusters.put(seedsRGB[seedIndex], rgb);
        }

        if (fillEmpySeeds) {
            // must not happen at last step;
            for (int f = 0; f < takenSeeds.length; f++) {
                if (!takenSeeds[f]) {
                    final HsvColor furthest = findFurthestColor(seedsHSV[f],
                            clusters.getLongest());
                    seedsHSV[f] = furthest;
                    clusters.put(cm.toRGB(furthest), cm.toRGB(furthest)
                            .toRGBInt());
                }
            }
        }

        return clusters;
    }

    public RgbColor[] extract(final int[] argbImage, final int paletteSize) {

        RgbColor[] seeds = makeSeeds(paletteSize);

        final List<Integer> colors = new ArrayList<Integer>(argbImage.length);
        for (final int i : argbImage) {
            colors.add(Integer.valueOf(i));
        }

        final int len = argbImage.length / paletteSize;
        for (int f = 0; f < paletteSize; f++) {
            final List<Integer> subList = colors.subList(f * len, (f + 1) * len);
            seeds[f] = ColorTools.blend(0.5f, ColorTools.meanColor(subList),
                    seeds[f]);
        }

        // 1) for each point find nearest seed
        // 2) move seeds to center of these groups
        // 3) remove weak groups
        MapOfLists<RgbColor, Integer> clusters = null;
        for (int f = 0; f < 4; f++) {
            clusters = assignColorsToSeeds(seeds, colors, true);
            seeds = moveSeeds(clusters);
        }

        clusters = assignColorsToSeeds(seeds, colors, false);
        seeds = moveSeedsToBest(clusters);

        return seeds;
    }

    private HsvColor findFurthestColor(final HsvColor seed, final List<Integer> argbImage) {
        int maxDistance = -1;
        HsvColor ret = null;
        for (final int rgb : argbImage) {

            final HsvColor rgbc = cm.toHSVColor(rgb);

            final int distance = cm.distance(seed, rgbc); // seed.distance(rgbc);
            if (distance > maxDistance) {

                maxDistance = distance;
                ret = rgbc;
            }
        }
        return ret;

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

    private RgbColor[] moveSeedsToBest(final MapOfLists<RgbColor, Integer> clusters) {
        final List<Entry<RgbColor, List<Integer>>> sorted = clusters
                .sortByListSize(false);

        final RgbColor[] seeds = new RgbColor[sorted.size()];

        for (int i = 0; i < seeds.length; i++) {
            final Entry<RgbColor, List<Integer>> e = sorted.get(i);
            final List<Integer> list = e.getValue();

            final RgbColor mean = ColorTools.bestColor(list, cm);

            seeds[i] = mean;
        }
        return seeds;
    }

    private HsvColor[] toHSV(final RgbColor[] seedsRGB) {
        final HsvColor[] seedsHSV = new HsvColor[seedsRGB.length];
        for (int f = 0; f < seedsRGB.length; f++) {
            seedsHSV[f] = cm.toHSVColor(seedsRGB[f]);
        }
        return seedsHSV;
    }

}
