package pe.edu.nova.plaza.audit.controller;

import java.nio.charset.StandardCharsets;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pe.edu.nova.plaza.audit.service.AuditService;

/**
 * Lee los eventos de {@code plaza.orders}, que Debezium publica como CloudEvents en modo binario (ADR-048), y los
 * guarda todos. Es la entrada del servicio por Kafka, como el controlador lo es por HTTP.
 *
 * <p>La traza continúa sola desde la cabecera {@code traceparent}.
 */
@Component
public class OrderEventsListener {

    private final AuditService audit;

    /**
     * Crea el listener.
     *
     * @param audit el servicio de auditoría
     */
    public OrderEventsListener(AuditService audit) {
        this.audit = audit;
    }

    /**
     * Guarda un evento de pedidos.
     *
     * @param payload     el valor del registro
     * @param id          {@code ce_id}
     * @param type        {@code ce_type}
     * @param source      {@code ce_source}
     * @param subject     {@code ce_subject}
     * @param time        {@code ce_time}
     * @param traceparent {@code ce_traceparent}, si viene
     */
    @KafkaListener(topics = "${plaza.audit.topic}")
    public void on(
            @Payload String payload,
            @Header("ce_id") byte[] id,
            @Header("ce_type") byte[] type,
            @Header("ce_source") byte[] source,
            @Header("ce_subject") byte[] subject,
            @Header("ce_time") byte[] time,
            @Header(name = "ce_traceparent", required = false) byte[] traceparent) {
        audit.record(new AuditService.ReceivedEvent(
                text(id), text(type), text(source), text(subject), text(time), text(traceparent), payload));
    }

    private static String text(byte[] header) {
        return header == null ? null : new String(header, StandardCharsets.UTF_8);
    }
}
