package com.adbrew.engine.config;

import com.adbrew.engine.seed.DemoIds;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI adbrewOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrueTake API")
                        .version("1.0.0")
                        .description("""
                                True campaign profitability after COGS, Amazon referral fees, \
                                FBA fulfillment, and returns, plus bid-change recommendations. \
                                No authentication; this is a demo."""));
    }

    /**
     * Swagger UI only renders a dropdown when the OpenAPI schema has {@code enum}.
     * Path {@code String} parameters otherwise stay as a free-text box.
     */
    @Bean
    public OpenApiCustomizer demoIdDropdowns() {
        return openApi -> {
            StringSchema accountId = enumString(DemoIds.ACCOUNT_IDS, DemoIds.ACC_LUMINA);
            StringSchema campaignId = enumString(DemoIds.CAMPAIGN_IDS, DemoIds.CAMP_SERUM);

            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getParameters() == null) {
                            return;
                        }
                        operation.getParameters().forEach(parameter -> {
                            if ("accountId".equals(parameter.getName())) {
                                parameter.setSchema(accountId);
                            }
                            if ("campaignId".equals(parameter.getName()) && "path".equals(parameter.getIn())) {
                                parameter.setSchema(campaignId);
                            }
                            if ("date".equals(parameter.getName())
                                    || "from".equals(parameter.getName())
                                    || "to".equals(parameter.getName())) {
                                parameter.setSchema(dateDropdown());
                            }
                        });
                    }));
        };
    }

    private static StringSchema dateDropdown() {
        return enumString(DemoIds.seededPerformanceDates(), DemoIds.latestSeededDate());
    }

    private static StringSchema enumString(List<String> values, String example) {
        StringSchema schema = new StringSchema();
        schema.setEnum(List.copyOf(values));
        schema.setExample(example);
        return schema;
    }
}
