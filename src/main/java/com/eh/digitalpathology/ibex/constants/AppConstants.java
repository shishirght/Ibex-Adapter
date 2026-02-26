/**
 * IbexConstants contains constant values which are used in whole ibex adapter application.
 * Author: Preeti Ankam
 * Date: November 01,2024
 */

package com.eh.digitalpathology.ibex.constants;

public final class AppConstants {


    private AppConstants() {
    }

    public static final String ACCESSION_NO_TAG_KEY = "00080050";
    public static final String SERIES_INSTANCE_UID = "0020000E";

    public static final String API_KEY_HEADER = "X-API-KEY";
    public static final String VALUE = "Value";

    public static final String SPECIMEN_DESCRIPTION_SEQUENCE = "00400560";
    public static final String CODE_MEANING = "00080104";
    public static final String CODE_VALUE = "00080100";

    public static final String PRIMARY_ANATOMIC_STRUCTURE_SEQ = "00082228";

    public static final String BAD_REQUEST = "400 Bad request";
    public static final String UNAUTHORIZED_ERROR = "401 Unauthorized: Check api key or authorization details";
    public static final String INTERNAL_SERVER_ERROR = "500 Internal Server Error";
    public static final String STATUS_CODE = "status code:: ";

    public static final String IBEX_SLIDE_CREATION_ERROR = "IBEX_SLIDE_CREATION_ERROR";


}
