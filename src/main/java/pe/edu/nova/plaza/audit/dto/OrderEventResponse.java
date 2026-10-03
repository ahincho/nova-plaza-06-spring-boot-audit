package pe.edu.nova.plaza.audit.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Un evento de la historia de un pedido.
 *
 * @param id          el id del evento
 * @param type        el tipo, como {@code pe.edu.nova.plaza.order.confirmed.v1}
 * @param time        cuándo pasó
 * @param traceparent la traza en que se creó, para buscarla en Grafana
 * @param data        el payload del evento
 */
public record OrderEventResponse(String id, String type, Instant time, String traceparent, Map<String, Object> data) {}
