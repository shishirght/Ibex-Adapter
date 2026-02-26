/**
 * SlideDetails is used to hold the details of slide.
 * Author: Preeti Ankam
 * Date: October 10, 2024
 */

package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public class SlideDetails {
    @JsonProperty("organ_type")
    private String organType;

    private String stain;

    @JsonProperty("stain_name")
    @Size(max = 255)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String stainName;

    //optional
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String procedure;

    @JsonProperty("sub_tissue")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subTissue;

    public String getOrganType() {
        return organType;
    }

    public void setOrganType(String organType) {
        this.organType = organType;
    }

    public String getStain() {
        return stain;
    }

    public SlideDetails setStain(String stain) {
        this.stain = stain;
        return this;
    }

    public String getStainName() {
        return stainName;
    }

    public void setStainName(String stainName) {
        this.stainName = stainName;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getSubTissue() {
        return subTissue;
    }

    public void setSubTissue(String subTissue) {
        this.subTissue = subTissue;
    }
}
