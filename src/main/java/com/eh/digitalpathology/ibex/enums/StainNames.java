/**
 * ValidStain is an enum of valid stains for slide creation.
 * Author: Preeti Ankam
 * Date:December 23, 2024
 */

package com.eh.digitalpathology.ibex.enums;

import java.util.List;

public enum StainNames {
    H_E("H&E", List.of("Hematoxylin & Eosin", "H&E IHC", "X HPV High Risk ISH")),
   // ER("ER", List.of("ER", "ER Breast Prognostic Marker", "X SW ER Control")),
    PR("PR", List.of("X SW PR Control", "PR Breast Prognostic Marker", "PR")),
    HER2("HER2", List.of("FISH - Her-2/neu", "X HER2 Neg Control", "HER-2/neu", "HER-2/neu Breast Prognostic Marker", "X Her-2/neu by In Situ Hybrdization")),
    KI67("Ki67", List.of("Ki-67", "Ki-67 Quantitative", "X SW Ki-67 Control", "X PanMelanoma+Ki-67 (LABEL ONLY)")),
    OTHER("Other", List.of());

    private final String stain;

    private final List<String> aliases;

    StainNames(String stain, List<String> aliases) {
        this.stain = stain;
        this.aliases = aliases;
    }


    public String getStain() {
        return stain;
    }

    public static StainNames fromStain(String input) {
        for (StainNames validStain : StainNames.values()) {
            if (validStain != OTHER) {
                for (String alias : validStain.aliases) {
                    if (alias.equalsIgnoreCase(input.trim())) {
                        return validStain;
                    }
                }
            }
        }
        return OTHER;
    }

}