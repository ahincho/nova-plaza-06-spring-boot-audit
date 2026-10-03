package pe.edu.nova.plaza.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** La auditoría de Plaza: guarda cada evento de pedidos en MongoDB y responde la historia de un pedido. */
@SpringBootApplication
public class AuditApplication {

    /** Para Spring. */
    protected AuditApplication() {}

    /**
     * Arranca el servicio.
     *
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
    }
}
