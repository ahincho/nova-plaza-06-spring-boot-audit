package pe.edu.nova.plaza.audit.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/** El reloj del servicio y qué pasa cuando un evento no se puede guardar. */
@Configuration(proxyBeanMethods = false)
public class AuditConfiguration {

    /** Para Spring. */
    public AuditConfiguration() {}

    /**
     * El reloj del servicio.
     *
     * @return el reloj en UTC
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Un evento que no se puede guardar se reintenta cada cinco segundos, sin límite, y bloquea su partición: nunca
     * se salta en silencio (ADR-048, regla 6). El log de cada intento fallido es la alerta.
     *
     * @return el manejador de errores del listener
     */
    @Bean
    CommonErrorHandler blockingErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(5_000, FixedBackOff.UNLIMITED_ATTEMPTS));
    }
}
