package com.eh.digitalpathology.ibex.client;

import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DBRestClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private DBRestClient dbRestClient;

    @BeforeEach
    void setUp() {
        dbRestClient = new DBRestClient(webClient);
        when(webClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    }

    @Test
    void exchange_withRequestBody_returnsResponse() {
        ApiResponse<String> apiResponse = new ApiResponse<>("success", "data", null, null);

        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        ParameterizedTypeReference<ApiResponse<String>> responseType = new ParameterizedTypeReference<>() {};

        ApiResponse<String> result = dbRestClient.exchange(HttpMethod.POST, "/test", "body", responseType, null).block();

        assertEquals(apiResponse, result);
    }

    @Test
    void exchange_withNullBody_returnsResponse() {
        ApiResponse<String> apiResponse = new ApiResponse<>("success", "data", null, null);

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        ParameterizedTypeReference<ApiResponse<String>> responseType = new ParameterizedTypeReference<>() {};

        ApiResponse<String> result = dbRestClient.exchange(HttpMethod.GET, "/test", null, responseType, null).block();

        assertEquals(apiResponse, result);
    }

    @Test
    void exchange_withHeadersConsumer_appliesHeaders() {
        ApiResponse<String> apiResponse = new ApiResponse<>("success", "data", null, null);

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        ParameterizedTypeReference<ApiResponse<String>> responseType = new ParameterizedTypeReference<>() {};

        ApiResponse<String> result = dbRestClient.exchange(HttpMethod.GET, "/test", null, responseType,
                headers -> headers.add("X-Custom", "value")).block();

        assertEquals(apiResponse, result);
        verify(requestBodySpec).headers(any());
    }

    @Test
    void exchange_onErrorStatus_withFailureStatus_throwsIbexApiException() {
        ApiResponse<String> errorResponse = new ApiResponse<>("failure", null, null, "something went wrong");

        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(errorResponse));

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenAnswer(inv -> {
            java.util.function.Predicate<HttpStatusCode> predicate = inv.getArgument(0);
            java.util.function.Function<ClientResponse, Mono<? extends Throwable>> handler = inv.getArgument(1);
            if (predicate.test(HttpStatusCode.valueOf(400))) {
                handler.apply(clientResponse).subscribe();
            }
            return responseSpec;
        });
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new IbexApiException("something went wrong")));

        ParameterizedTypeReference<ApiResponse<String>> responseType = new ParameterizedTypeReference<>() {};

        Mono<ApiResponse<String>> mono = dbRestClient.exchange(HttpMethod.GET, "/test", null, responseType, null);

        Throwable thrown = assertThrows(Throwable.class, mono::block);
        Throwable cause = thrown.getCause() != null ? thrown.getCause() : thrown;
        assertInstanceOf(IbexApiException.class, cause);
    }

    @Test
    void exchange_onErrorStatus_withNonFailureStatus_completesEmpty() {
        ApiResponse<String> errorResponse = new ApiResponse<>("error", null, null, "ignored");

        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(errorResponse));

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenAnswer(inv -> {
            java.util.function.Predicate<HttpStatusCode> predicate = inv.getArgument(0);
            java.util.function.Function<ClientResponse, Mono<? extends Throwable>> handler = inv.getArgument(1);
            if (predicate.test(HttpStatusCode.valueOf(400))) {
                handler.apply(clientResponse).subscribe();
            }
            return responseSpec;
        });
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.empty());

        ParameterizedTypeReference<ApiResponse<String>> responseType = new ParameterizedTypeReference<>() {};

        ApiResponse<String> result = dbRestClient.exchange(HttpMethod.GET, "/test", null, responseType, null).block();

        assertNull(result);
    }
}