/**
 * Slide is used to hold the data regarding Slides.
 * Author: Preeti Ankam
 * Date: October 10, 2024
 */

package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Slide {
    private String id;

    @JsonProperty("case_id")
    private String caseId;

    private String source;

    private SlideDetails details;

    //optional
    @JsonProperty("file_extension")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String fileExt;

    @JsonProperty("scanned_at")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String scannedDate;

    public Slide(){
        this.details = new SlideDetails();
    }


    public String getSource ( ) {
        return source;
    }

    public void setSource ( String source ) {
        this.source = source;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getScannedDate() {
        return scannedDate;
    }

    public void setScannedDate(String scannedDate) {
        this.scannedDate = scannedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public SlideDetails getDetails ( ) {
        return details;
    }
}
