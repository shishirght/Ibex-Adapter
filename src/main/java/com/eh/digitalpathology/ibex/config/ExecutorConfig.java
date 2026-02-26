package com.eh.digitalpathology.ibex.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {

    private static final Logger log = LoggerFactory.getLogger( ExecutorConfig.class );

    private ExecutorService ibexExecutorService;

    @Bean( name = "ibexExecutorService" )
    public ExecutorService ibexExecutorService ( ) {
        this.ibexExecutorService = Executors.newScheduledThreadPool( Runtime.getRuntime( ).availableProcessors( ) );
        return this.ibexExecutorService;
    }
    @PreDestroy
    public void shutdownExecutors ( ) {
        shutdownExecutor( ibexExecutorService, "Ibex ExecutorService" );
    }

    private void shutdownExecutor ( ExecutorService executor, String name ) {
        if ( executor != null ) {
            executor.shutdown( );
            try {
                if ( !executor.awaitTermination( 60, TimeUnit.SECONDS ) ) {
                    executor.shutdownNow( );
                    if ( !executor.awaitTermination( 60, TimeUnit.SECONDS ) ) {
                        log.error( "{} did not terminate", name );
                    }
                }
            } catch ( InterruptedException ex ) {
                executor.shutdownNow( );
                Thread.currentThread( ).interrupt( );
            }
        }
    }
}
