package pe.edu.nova.plaza.audit.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.nova.plaza.audit.dto.OrderEventResponse;
import pe.edu.nova.plaza.audit.service.AuditService;

/**
 * La historia de un pedido. No comprueba de quién es el pedido: los eventos no llevan el cliente, y quien lo
 * comprueba es el BFF, contra pedidos, antes de pedir la historia (ADR-048).
 */
@RestController
@RequestMapping("/v1/orders")
public class OrderHistoryController {

    private final AuditService audit;

    /**
     * Crea el controlador.
     *
     * @param audit el servicio de auditoría
     */
    public OrderHistoryController(AuditService audit) {
        this.audit = audit;
    }

    /**
     * Los eventos de un pedido.
     *
     * @param id el pedido
     * @return del más viejo al más nuevo; vacía si el pedido no tiene eventos
     */
    @GetMapping("/{id}/events")
    public List<OrderEventResponse> events(@PathVariable String id) {
        return audit.history(id).stream()
                .map(event -> new OrderEventResponse(
                        event.id(), event.type(), event.time(), event.traceparent(), event.data()))
                .toList();
    }
}
