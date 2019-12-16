package io.projectriff.orderprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.projectriff.inventory.Article;
import io.projectriff.orders.OrderEvent;
import io.projectriff.orders.ProcessedOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class InventoryUpdateFunction implements Function<Flux<OrderEvent>, Tuple2<Flux<ProcessedOrderEvent>, Flux<ProcessedOrderEvent>>> {

	private static String INVENTORY_API = "http://inventory-api.default.svc.cluster.local";

	private final Logger logger = LoggerFactory.getLogger(InventoryUpdateFunction.class);

	private RestTemplate restTemplate;

	public InventoryUpdateFunction() {
		this.restTemplate = new RestTemplateBuilder().build();
	}

	public InventoryUpdateFunction(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public Tuple2<Flux<ProcessedOrderEvent>, Flux<ProcessedOrderEvent>> apply(Flux<OrderEvent> orders) {
		ConnectableFlux<ProcessedOrderEvent> processed = orders.flatMap(o-> checkInventory(o)).publish();
		Flux<ProcessedOrderEvent> fulfillments = processed.filter(o -> o.getStatus().equals("fulfill"));
		Flux<ProcessedOrderEvent> backorders = processed.filter(o -> o.getStatus().equals("backorder"));
		processed.connect();
		return Tuples.of(fulfillments, backorders);
	}

	private Flux<ProcessedOrderEvent> checkInventory(OrderEvent o) {
		logger.info("received order event for " + o.getUser());
		ProcessedOrderEvent fulfillment = new ProcessedOrderEvent();
		fulfillment.setStatus("fulfill");
		fulfillment.setUser(o.getUser());
		fulfillment.setProducts(new HashMap<>());
		ProcessedOrderEvent backorder = new ProcessedOrderEvent();
		backorder.setStatus("backorder");
		backorder.setUser(o.getUser());
		backorder.setProducts(new HashMap<>());
		for (Map.Entry<String, Integer> entry : o.getProducts().entrySet()) {
			if (entry.getValue() != 0) {
				int quantityInInventory = 0;
				Article item = getArticle(entry);
				logger.info("inventory item: " + item);
				if (item != null) {
					quantityInInventory = item.getQuantity();
				}
				if (quantityInInventory < entry.getValue()) {
					logger.info("not enough in stock - backordering " + entry.getKey() + " for " + o.getUser());
					backorder.getProducts().put(entry.getKey(), entry.getValue());
				} else {
					int newQuantity = quantityInInventory - entry.getValue();
					logger.info("update inventory for " + entry.getKey() + " from=" + quantityInInventory + " to=" + newQuantity);
					//ToDo: add retry since inventory might have changed during this order processing
					Integer count = updateQuantityInStock(entry, quantityInInventory, newQuantity);
					if (count == null || count.equals(0)) {
						logger.info("failed update - backordering " + entry.getKey() + " for " + o.getUser());
						backorder.getProducts().put(entry.getKey(), entry.getValue());
					} else {
						logger.info("fulfilling item " + entry.getKey() + " with a quantity of " + entry.getValue() + " for " + o.getUser());
						fulfillment.getProducts().put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		List<ProcessedOrderEvent> processed = new ArrayList<>();
		if (fulfillment.getProducts().size() > 0) {
			processed.add(fulfillment);
		}
		if (backorder.getProducts().size() > 0) {
			processed.add(backorder);
		}
		return Flux.fromIterable(processed);
	}

	private Integer updateQuantityInStock(Map.Entry<String, Integer> entry, int quantityInInventory, int newQuantity) {
		logger.debug("GET " + INVENTORY_API + "/api/article/search/updateBySku?sku=" + entry.getKey() +
				"&from=" + quantityInInventory + "&to=" + newQuantity);
		return restTemplate.getForObject(
				INVENTORY_API + "/api/article/search/updateBySku?sku=" + entry.getKey() +
						"&from=" + quantityInInventory + "&to=" + newQuantity, Integer.class);
	}

	private Article getArticle(Map.Entry<String, Integer> entry) {
		logger.debug("GET " + INVENTORY_API + "/api/article/search/findBySku?sku=" + entry.getKey());
		return this.restTemplate.getForObject(
				INVENTORY_API + "/api/article/search/findBySku?sku=" + entry.getKey(), Article.class);
	}
}