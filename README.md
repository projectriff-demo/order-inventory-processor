# Order-Inventory Processor

Build the Function:

```
riff function create order-inventory \
  --git-repo https://github.com/projectriff-demo/order-inventory-processor \
  --handler io.projectriff.orderprocessor.InventoryUpdateFunction
  --tail
```

Create the Streams:

```
riff streaming stream create orders \
    --provider franz-kafka-provisioner \
    --content-type application/json

riff streaming stream create fulfillments \
    --provider franz-kafka-provisioner \
    --content-type application/json

riff streaming stream create backorders \
    --provider franz-kafka-provisioner \
    --content-type application/json
```

Create the Processor:

```
riff streaming processor create order-inventory \
    --function-ref order-inventory \
    --input orders \
    --output fulfillments \
    --output backorders \
    --tail
```
