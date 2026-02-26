package com.eh.digitalpathology.ibex.service;

import org.springframework.stereotype.Component;

@Component
public class BucketContext {

    private String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName){
        this.bucketName=bucketName;
    }
}
