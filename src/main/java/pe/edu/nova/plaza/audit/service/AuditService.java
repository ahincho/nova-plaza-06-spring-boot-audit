package pe.edu.nova.plaza.audit.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import pe.edu.nova.plaza.audit.entity.OrderEvent;
import pe.edu.nova.plaza.audit.repository.OrderEventRepository;

/** Guarda los eventos de pedidos y responde la historia de cada uno. */
@Service
public class AuditService {

    private final MongoTemplate mongo;
    private final OrderEventRepository events;
    private final Clock clock;

    /**
     * Crea el servicio.
     *
     * @param mongo  la base, para insertar sin pisar
     * @param events los eventos guardados
     * @param clock  el reloj del servicio
     */
    public AuditService(MongoTemplate mongo, OrderEventRepository events, Clock clock) {
        this.mongo = mongo;
        this.events = events;
        this.clock = clock;
    }

    /**
     * Guarda un evento. Es una inserción y no un upsert: un evento repetido choca con su id y se descarta, sin
     * pisar el que ya estaba.
     *
     * @param event el evento, como llegó por Kafka
     * @return {@code true} si lo guardó, y {@code false} si ya estaba
     */
    public boolean record(ReceivedEvent event) {
        Map<String, Object> data = Document.parse(event.payload());
        try {
            mongo.insert(new OrderEvent(
                    event.id(),
                    event.type(),
                    event.source(),
                    event.orderId(),
                    Instant.parse(event.time()),
                    event.traceparent(),
                    data,
                    clock.instant()));
            return true;
        } catch (DuplicateKeyException repeated) {
            return false;
        }
    }

    /**
     * La historia de un pedido.
     *
     * @param orderId el pedido
     * @return sus eventos, del más viejo al más nuevo; vacía si no hay ninguno
     */
    public List<OrderEvent> history(String orderId) {
        return events.findByOrderIdOrderByTimeAsc(orderId);
    }

    /**
     * Un evento como llega por Kafka, con las cabeceras de CloudEvents ya leídas.
     *
     * @param id          {@code ce_id}
     * @param type        {@code ce_type}
     * @param source      {@code ce_source}
     * @param orderId     {@code ce_subject}
     * @param time        {@code ce_time}, en ISO-8601
     * @param traceparent {@code ce_traceparent}, o {@code null}
     * @param payload     el valor del registro, en JSON
     */
    public record ReceivedEvent(
            String id, String type, String source, String orderId, String time, String traceparent, String payload) {}
}
