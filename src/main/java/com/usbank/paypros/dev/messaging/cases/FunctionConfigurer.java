package com.usbank.paypros.dev.messaging.cases;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class FunctionConfigurer {

    @Bean
    @Description("validateAccountID")
    public Function<AccountID, Boolean> validateAccountID() {
        return s -> List.of(
                "0014729991743821",
                "0041749992614938"
        ).contains(s.value);
    }

    @Bean
    @Description("validateCreatedBy")
    public Function<CreatedBy, Boolean> validateCreatedBy() {
        return s -> Boolean.TRUE;
    }

    public record AccountID(String value) {}

    public record CreatedBy(String id) {}
}
