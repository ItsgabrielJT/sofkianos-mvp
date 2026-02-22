package com.sofkianos.consumer.architecture;

import com.sofkianos.consumer.component.KudosConsumer;
import com.sofkianos.consumer.config.RabbitConfig;
import com.sofkianos.consumer.domain.events.KudoEvent;
import com.sofkianos.consumer.service.KudoService;
import com.sofkianos.consumer.service.impl.KudoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architecture tests for US-007 — Typed Deserialization in Consumer.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>TC-R07-002 — KudosConsumer and KudoService signatures use KudoEvent (not String)</li>
 *   <li>TC-R07-003 — Jackson2JsonMessageConverter registered as @Bean in RabbitConfig</li>
 *   <li>TC-R07-005 — KudoServiceImpl has NO ObjectMapper, JsonNode, or readTree()</li>
 * </ul>
 */
class ConsumerTypedDeserializationArchTest {

    // ── TC-R07-002 — KudosConsumer.handleKudo() uses KudoEvent, not String ──

    @Test
    @DisplayName("TC-R07-002 — KudosConsumer.handleKudo() first parameter is KudoEvent")
    void handleKudo_parameterIsKudoEvent() throws NoSuchMethodException {
        Method handleKudo = KudosConsumer.class.getDeclaredMethod("handleKudo", KudoEvent.class);
        Parameter firstParam = handleKudo.getParameters()[0];

        assertThat(firstParam.getType())
                .as("handleKudo first parameter must be KudoEvent, not String")
                .isEqualTo(KudoEvent.class);
    }

    @Test
    @DisplayName("TC-R07-002 — KudoService.saveKudo() first parameter is KudoEvent")
    void saveKudo_parameterIsKudoEvent() throws NoSuchMethodException {
        Method saveKudo = KudoService.class.getDeclaredMethod("saveKudo", KudoEvent.class);
        Parameter firstParam = saveKudo.getParameters()[0];

        assertThat(firstParam.getType())
                .as("saveKudo first parameter must be KudoEvent, not String")
                .isEqualTo(KudoEvent.class);
    }

    @Test
    @DisplayName("TC-R07-002 — KudosConsumer has NO method accepting String payload")
    void konsumer_hasNoStringPayloadMethod() {
        boolean hasStringMethod = Arrays.stream(KudosConsumer.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("handleKudo"))
                .anyMatch(m -> m.getParameterCount() == 1
                        && m.getParameterTypes()[0] == String.class);

        assertThat(hasStringMethod)
                .as("KudosConsumer must NOT have handleKudo(String) — Primitive Obsession eliminated")
                .isFalse();
    }

    // ── TC-R07-003 — Jackson2JsonMessageConverter registered as @Bean ──

    @Test
    @DisplayName("TC-R07-003 — RabbitConfig has @Bean returning MessageConverter")
    void rabbitConfig_hasMessageConverterBean() {
        boolean hasConverterBean = Arrays.stream(RabbitConfig.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .anyMatch(m -> MessageConverter.class.isAssignableFrom(m.getReturnType()));

        assertThat(hasConverterBean)
                .as("RabbitConfig must declare a @Bean of type MessageConverter")
                .isTrue();
    }

    @Test
    @DisplayName("TC-R07-003 — MessageConverter @Bean returns Jackson2JsonMessageConverter instance")
    void rabbitConfig_messageConverterIsJackson2Json() {
        RabbitConfig config = new RabbitConfig();

        // Find the @Bean method that returns MessageConverter
        MessageConverter converter = Arrays.stream(RabbitConfig.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .filter(m -> MessageConverter.class.isAssignableFrom(m.getReturnType()))
                .map(m -> {
                    try {
                        m.setAccessible(true);
                        return (MessageConverter) m.invoke(config);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No MessageConverter @Bean found"));

        assertThat(converter)
                .as("MessageConverter must be Jackson2JsonMessageConverter")
                .isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    // ── TC-R07-005 — KudoServiceImpl has NO ObjectMapper / JsonNode ──

    @Test
    @DisplayName("TC-R07-005 — KudoServiceImpl has NO fields of type ObjectMapper or JsonNode")
    void serviceImpl_hasNoJacksonFields() {
        Field[] fields = KudoServiceImpl.class.getDeclaredFields();

        for (Field field : fields) {
            String typeName = field.getType().getName();
            assertThat(typeName)
                    .as("KudoServiceImpl field '%s' must not be ObjectMapper", field.getName())
                    .doesNotContain("ObjectMapper");
            assertThat(typeName)
                    .as("KudoServiceImpl field '%s' must not be JsonNode", field.getName())
                    .doesNotContain("JsonNode");
        }
    }

    @Test
    @DisplayName("TC-R07-005 — KudoServiceImpl constructors have NO ObjectMapper parameter")
    void serviceImpl_noObjectMapperInConstructor() {
        Arrays.stream(KudoServiceImpl.class.getDeclaredConstructors())
                .forEach(constructor -> {
                    for (Parameter param : constructor.getParameters()) {
                        assertThat(param.getType().getName())
                                .as("Constructor parameter must not be ObjectMapper")
                                .doesNotContain("ObjectMapper");
                    }
                });
    }
}
