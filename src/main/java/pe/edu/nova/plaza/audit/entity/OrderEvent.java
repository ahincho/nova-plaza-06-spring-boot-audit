package pe.edu.nova.plaza.audit.entity;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Un evento de un pedido, tal como llegó. El id es el {@code ce_id} del evento: un evento repetido choca con la
 * clave y no se guarda dos veces (ADR-048, regla 10).
 *
 * @param id          el {@code ce_id}
 * @param type        el {@code ce_type}
 * @param source      el {@code ce_source}
 * @param orderId     el {@code ce_subject}, el id del pedido
 * @param time        el {@code ce_time}, cuándo pasó en pedidos
 * @param traceparent el contexto de la traza en que se creó, si lo tiene
 * @param data        el payload del evento
 * @param receivedAt  cuándo lo guardó la auditoría
 */
@Document("order_events")
public record OrderEvent(
        @Id String id,
        String type,
        String source,
        @Indexed String orderId,
        Instant time,
        String traceparent,
        Map<String, Object> data,
        Instant receivedAt) {}
