package com.eh.digitalpathology.ibex.client;

import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DBRestClientTest {

    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    private DBRestClient dbRestClient;

    @BeforeEach
    void setup() {

        webClient = mock(WebClient.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        dbRestClient = new DBRestClient(webClient);

        when(webClient.method(any(HttpMethod.class)))
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(anyString()))
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.body(any()))
                .thenReturn(requestHeadersSpec);

        when(requestBodyUriSpec.headers(any()))
                .thenReturn(requestBodyUriSpec);

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);

        when(requestBodyUriSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);
    }

    @Test
    void success_withBody() {

        ApiResponse<String> response = mock(ApiResponse.class);
        when(response.status()).thenReturn("success");

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(response));

        ApiResponse<String> result =
                dbRestClient.exchange(
                        HttpMethod.POST,
                        "/test",
                        "body",
                        new ParameterizedTypeReference<ApiResponse<String>>() {},
                        null
                ).block();

        assertNotNull(result);
        assertEquals("success", result.status());
    }

    @Test
    void success_withoutBody() {

        ApiResponse<String> response = mock(ApiResponse.class);
        when(response.status()).thenReturn("success");

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(response));

        ApiResponse<String> result =
                dbRestClient.exchange(
                        HttpMethod.GET,
                        "/test",
                        null,
                        new ParameterizedTypeReference<ApiResponse<String>>() {},
                        null
                ).block();

        assertNotNull(result);
        assertEquals("success", result.status());
    }

    @Test
    void throwsException_whenFailureStatus() {

        ArgumentCaptor<Function<ClientResponse, Mono<? extends Throwable>>> captor =
                ArgumentCaptor.forClass(Function.class);

        when(responseSpec.onStatus(any(), captor.capture()))
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.empty());

        dbRestClient.exchange(
                HttpMethod.GET,
                "/test",
                null,
                new ParameterizedTypeReference<ApiResponse<String>>() {},
                null
        );

        ApiResponse<String> errorResponse = mock(ApiResponse.class);
        when(errorResponse.status()).thenReturn("failure");
        when(errorResponse.errorMessage()).thenReturn("Some error");

        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(errorResponse));

        Mono<? extends Throwable> errorMono =
                captor.getValue().apply(clientResponse);

        assertThrows(IbexApiException.class, errorMono::block);
    }

    @Test
    void noException_whenNonFailureStatus() {

        ArgumentCaptor<Function<ClientResponse, Mono<? extends Throwable>>> captor =
                ArgumentCaptor.forClass(Function.class);

        when(responseSpec.onStatus(any(), captor.capture()))
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.empty());

        dbRestClient.exchange(
                HttpMethod.GET,
                "/test",
                null,
                new ParameterizedTypeReference<ApiResponse<String>>() {},
                null
        );

        ApiResponse<String> errorResponse = mock(ApiResponse.class);
        when(errorResponse.status()).thenReturn("error");

        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(errorResponse));

        Mono<? extends Throwable> errorMono =
                captor.getValue().apply(clientResponse);

        assertNull(errorMono.block());
    }
}