package com.eh.digitalpathology.ibex.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecutorConfigTest {

    private ExecutorConfig executorConfig;

    @BeforeEach
    void setUp() {
        executorConfig = new ExecutorConfig();
    }

    @Test
    void ibexExecutorService_returnsNonNullExecutor() {
        ExecutorService service = executorConfig.ibexExecutorService();
        assertNotNull(service);
        service.shutdown();
    }

    @Test
    void shutdownExecutors_whenExecutorTerminatesGracefully() throws Exception {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(60, TimeUnit.SECONDS)).thenReturn(true);

        executorConfig.ibexExecutorService();

        injectExecutor(executorConfig, mockExecutor);

        executorConfig.shutdownExecutors();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).awaitTermination(60, TimeUnit.SECONDS);
        verify(mockExecutor, never()).shutdownNow();
    }

    @Test
    void shutdownExecutors_whenFirstAwaitTimeoutThenSecondSucceeds() throws Exception {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(60, TimeUnit.SECONDS))
                .thenReturn(false)
                .thenReturn(true);

        executorConfig.ibexExecutorService();
        injectExecutor(executorConfig, mockExecutor);

        executorConfig.shutdownExecutors();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
        verify(mockExecutor, times(2)).awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    void shutdownExecutors_whenBothAwaitsTimeout_logsError() throws Exception {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(60, TimeUnit.SECONDS)).thenReturn(false);

        executorConfig.ibexExecutorService();
        injectExecutor(executorConfig, mockExecutor);

        executorConfig.shutdownExecutors();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
        verify(mockExecutor, times(2)).awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    void shutdownExecutors_whenInterruptedExceptionThrown_shutsDownNowAndInterruptsThread() throws Exception {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        when(mockExecutor.awaitTermination(60, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("interrupted"));

        executorConfig.ibexExecutorService();
        injectExecutor(executorConfig, mockExecutor);

        executorConfig.shutdownExecutors();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // clear interrupt flag after test
    }

    @Test
    void shutdownExecutors_whenExecutorIsNull_doesNothing() {
        // ibexExecutorService() never called, field stays null
        assertDoesNotThrow(() -> executorConfig.shutdownExecutors());
    }

    private void injectExecutor(ExecutorConfig config, ExecutorService executor) throws Exception {
        var field = ExecutorConfig.class.getDeclaredField("ibexExecutorService");
        field.setAccessible(true);
        field.set(config, executor);
    }
}