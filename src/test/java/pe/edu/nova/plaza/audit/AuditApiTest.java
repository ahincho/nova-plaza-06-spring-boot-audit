package pe.edu.nova.plaza.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.vault.VaultContainer;
import pe.edu.nova.plaza.audit.controller.OrderEventsListener;
import pe.edu.nova.plaza.audit.repository.OrderEventRepository;

/**
 * La auditoría contra un MongoDB y un Vault reales. Las pruebas no levantan Kafka: le entregan al listener el
 * registro con las cabeceras de CloudEvents, como lo publica Debezium.
 */
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@AutoConfigureMockMvc
class AuditApiTest {

    private static final String VAULT_TOKEN = "plaza-test-root";
    private static final String TRACEPARENT = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";

    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0.32");
    private static final VaultContainer<?> VAULT =
            new VaultContainer<>("hashicorp/vault:2.1.1").withVaultToken(VAULT_TOKEN);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderEventsListener listener;

    @Autowired
    private OrderEventRepository events;

    @BeforeAll
    static void infrastructure() throws IOException, InterruptedException {
        MONGO.start();
        VAULT.start();
        // El secreto lleva la URI igual que el compose de Plaza: el servicio la lee de Vault, como en producción.
        VAULT.execInContainer(
                "vault", "kv", "put", "secret/plaza/audit/mongo", "MONGODB_URI=" + MONGO.getReplicaSetUrl("audit"));
        System.setProperty("nova.secrets.vault.address", VAULT.getHttpHostAddress());
        System.setProperty("nova.secrets.vault.token", VAULT_TOKEN);
    }

    @Test
    void eachEventOfAnOrderIsStoredOnceAndItsHistoryComesInOrder() throws Exception {
        String order = UUID.randomUUID().toString();
        String created = UUID.randomUUID().toString();
        String confirmed = UUID.randomUUID().toString();

        receive(confirmed, "pe.edu.nova.plaza.order.confirmed.v1", order, "2026-10-02T15:00:01Z", TRACEPARENT);
        receive(created, "pe.edu.nova.plaza.order.created.v1", order, "2026-10-02T15:00:00Z", null);
        receive(confirmed, "pe.edu.nova.plaza.order.confirmed.v1", order, "2026-10-02T15:00:01Z", TRACEPARENT);

        assertThat(events.findByOrderIdOrderByTimeAsc(order)).hasSize(2);
        mvc.perform(get("/v1/orders/{id}/events", order))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(created))
                .andExpect(jsonPath("$.data[0].type").value("pe.edu.nova.plaza.order.created.v1"))
                .andExpect(jsonPath("$.data[1].id").value(confirmed))
                .andExpect(jsonPath("$.data[1].traceparent").value(TRACEPARENT))
                .andExpect(jsonPath("$.data[1].data.orderId").value(order))
                .andExpect(jsonPath("$.data[1].data.items[0].sku").value("MUG-001"));
    }

    @Test
    void anOrderWithoutEventsHasAnEmptyHistory() throws Exception {
        mvc.perform(get("/v1/orders/{id}/events", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private void receive(String id, String type, String order, String time, String traceparent) {
        String payload = "{\"orderId\":\"" + order + "\",\"items\":[{\"sku\":\"MUG-001\",\"quantity\":2}]}";
        listener.on(
                payload,
                bytes(id),
                bytes(type),
                bytes("/plaza/orders"),
                bytes(order),
                bytes(time),
                traceparent == null ? null : bytes(traceparent));
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
