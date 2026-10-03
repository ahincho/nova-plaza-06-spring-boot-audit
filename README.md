# nova-plaza-06-spring-boot-audit

La auditoría de [Plaza](https://github.com/ahincho/nova-plaza-01-shared-platform), en Spring Boot. Guarda en
MongoDB cada evento que publica pedidos —creado, confirmado y cancelado— y responde la historia de un pedido.

La decisión está en [ADR-048](https://github.com/ahincho/nova-shared-01-docs/blob/main/adrs/shared/ADR-048-outbox-transaccional-detras-de-un-contrato.md):
pedidos escribe el evento en su outbox, Debezium lo publica en `plaza.orders` como CloudEvent, y la auditoría
lo lee de ahí.

## Qué hace

| Entrada | Qué hace |
|---|---|
| `plaza.orders` en Kafka | guarda cada evento como un documento de `order_events`, con `_id` igual a `ce_id`: uno repetido choca con la clave y se descarta |
| `GET /v1/orders/{id}/events` | la historia del pedido, del evento más viejo al más nuevo, en el sobre del estándar de API |

Los eventos no llevan el cliente, así que la auditoría no sabe de quién es un pedido. El BFF lo comprueba
contra pedidos antes de pedir la historia.

Un evento que no se puede guardar se reintenta cada cinco segundos, sin límite, y bloquea su partición: nunca
se salta. El log de cada intento fallido es la alerta. La traza continúa desde la cabecera `traceparent`, así
que la compra se ve en Grafana como una sola traza, del BFF a pedidos y de ahí a la auditoría.

## Levantarlo en local

Requiere el compose de [`nova-plaza-01-shared-platform`](https://github.com/ahincho/nova-plaza-01-shared-platform),
que levanta MongoDB, Kafka con Debezium y Vault con el secreto `secret/plaza/audit/mongo`.

```bash
VAULT_ADDR=http://localhost:8200 VAULT_TOKEN=plaza-local-root ./gradlew bootRun
```

Escucha en el puerto 8085.

## Licencia

[Eclipse Public License 2.0](LICENSE).
