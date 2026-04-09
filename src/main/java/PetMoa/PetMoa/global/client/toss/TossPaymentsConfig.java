package PetMoa.PetMoa.global.client.toss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class TossPaymentsConfig {

    @Value("${toss.payments.secret-key:test_sk_xxx}")
    private String secretKey;

    @Value("${toss.payments.base-url:https://api.tosspayments.com}")
    private String baseUrl;

    @Bean
    public RestClient tossPaymentsRestClient() {
        String credentials = secretKey + ":";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
