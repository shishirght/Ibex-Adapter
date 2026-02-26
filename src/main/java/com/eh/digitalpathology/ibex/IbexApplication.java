/**
 * IbexApplication is the entry point of the ibex adapter service which contains the main method.
 * Author: Preeti Ankam
 * Date: October 30, 2024
 */

package com.eh.digitalpathology.ibex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IbexApplication {
    public static void main(String[] args) {
        SpringApplication.run(IbexApplication.class, args);
    }
}


