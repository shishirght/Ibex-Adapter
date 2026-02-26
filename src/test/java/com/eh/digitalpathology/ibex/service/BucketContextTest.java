package com.eh.digitalpathology.ibex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BucketContextTest {

    private BucketContext bucketContext;

    @BeforeEach
    void setUp() {
        bucketContext = new BucketContext();
    }

    @Test
    void getBucketName_shouldReturnNullInitially() {
        // Act
        String bucketName = bucketContext.getBucketName();

        // Assert
        assertNull(bucketName);
    }

    @Test
    void setBucketName_shouldStoreAndReturnValue() {
        // Arrange
        String bucket = "test-bucket";

        // Act
        bucketContext.setBucketName(bucket);

        // Assert
        assertEquals(bucket, bucketContext.getBucketName());
    }

    @Test
    void setBucketName_shouldOverwriteExistingValue() {
        // Arrange
        bucketContext.setBucketName("bucket-1");

        // Act
        bucketContext.setBucketName("bucket-2");

        // Assert
        assertEquals("bucket-2", bucketContext.getBucketName());
    }
}
