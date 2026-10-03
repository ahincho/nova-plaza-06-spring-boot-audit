package pe.edu.nova.plaza.audit.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import pe.edu.nova.plaza.audit.entity.OrderEvent;

/** Los eventos guardados. */
public interface OrderEventRepository extends MongoRepository<OrderEvent, String> {

    /**
     * La historia de un pedido.
     *
     * @param orderId el pedido
     * @return sus eventos, del más viejo al más nuevo
     */
    List<OrderEvent> findByOrderIdOrderByTimeAsc(String orderId);
}
