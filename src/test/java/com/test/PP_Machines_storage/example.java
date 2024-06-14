package com.test.PP_Machines_storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class example {
    public static void main(String[] args) {
        // Expected formats
        Set<String> expectedFormats = new HashSet<>();
        expectedFormats.add("compressed.jp2");
        expectedFormats.add("dirInfo.txt");
        expectedFormats.add("downsampled.tif");
        expectedFormats.add("lossless.jp2");
        expectedFormats.add("macro.jpg");
        expectedFormats.add("thumbnail.jpg");
        expectedFormats.add("thumbnail_original.jpg");
        expectedFormats.add("thumbnail_small.jpg");
        expectedFormats.add("label.jpg");

        // Provided formats
        Set<String> providedFormats = new HashSet<>();
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_compressed.jp2");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_dirInfo.txt");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_downsampled.tif");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_lossless.jp2");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_macro.jpg");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_thumbnail.jpg");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_thumbnail_original.jpg");
        providedFormats.add("B_260_HB17[LL]-SL_95-ST_NISL-SE_283_thumbnail_small.jpg");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_compressed.jp2");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_dirInfo.txt");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_downsampled.tif");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_lossless.jp2");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_thumbnail.jpg");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_thumbnail_original.jpg");
        providedFormats.add("B_260_HB17[LL]-SL_98-ST_NISL-SE_292_thumbnail_small.jpg");

        // Extract section numbers and group provided formats by section
        Map<String, Set<String>> sectionFormatsMap = new HashMap<>();
        for (String format : providedFormats) {
            int sectionStart = format.indexOf("SE_") + 3;
            int sectionEnd = format.indexOf('_', sectionStart);
            if (sectionEnd == -1) {
                sectionEnd = format.indexOf('-', sectionStart);
            }
            if (sectionStart != -1 && sectionEnd != -1) {
                String sectionNumber = format.substring(sectionStart, sectionEnd);
                sectionFormatsMap.putIfAbsent(sectionNumber, new HashSet<String>());
                for (String expected : expectedFormats) {
                    if (format.endsWith(expected)) {
                        sectionFormatsMap.get(sectionNumber).add(expected);
                        break;
                    }
                }
            }
        }

        // Check for missing formats in each section
        for (Map.Entry<String, Set<String>> entry : sectionFormatsMap.entrySet()) {
            String sectionNumber = entry.getKey();
            Set<String> providedSuffixes = entry.getValue();
            Set<String> missingFormats = new HashSet<>(expectedFormats);
            missingFormats.removeAll(providedSuffixes);
            System.out.println("Section " + sectionNumber + " missing formats: " + missingFormats);
        }
    }
}
