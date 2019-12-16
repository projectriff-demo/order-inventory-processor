package io.projectriff.orderprocessor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;

import io.projectriff.inventory.Article;
import io.projectriff.inventory.ArticleQuantityUpdate;
import io.projectriff.orders.OrderEvent;
import io.projectriff.orders.ProcessedOrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class OrderInventoryTests {

	@Mock
	private RestTemplate restTemplate;

	@Test
	public void test() throws InterruptedException {
		Article widget = new Article("widget", "Widget", "Test1", BigDecimal.TEN, null, 5, null);
		Article gadget = new Article("gadget", "Gadget", "Test2", BigDecimal.ONE, null, 1, null);
		Article updatedWidget = new Article("gadget", "Gadget", "Test2", BigDecimal.ONE, null, 2, null);
		HttpEntity<ArticleQuantityUpdate> requestUpdate =  new HttpEntity<>(new ArticleQuantityUpdate(5, 2));
		Mockito.when(restTemplate.getForObject(
				"http://inventory-api.default.svc.cluster.local/api/article/search/findBySku?sku=widget", Article.class))
				.thenReturn(widget);
		Mockito.when(restTemplate.patchForObject(
				"http://inventory-api.default.svc.cluster.local/api/article/updateQuantityBySku?sku=widget",
				requestUpdate,
				Article.class))
				.thenReturn(updatedWidget);
		Mockito.when(restTemplate.getForObject(
				"http://inventory-api.default.svc.cluster.local/api/article/search/findBySku?sku=gadget", Article.class))
				.thenReturn(gadget);

		OrderEvent event1 = new OrderEvent();
		event1.setUser("homer");
		event1.setProducts(new HashMap<>());
		event1.getProducts().put("widget", 3);
		event1.getProducts().put("gadget", 7);

		Flux<OrderEvent> orders = Flux.fromIterable(Collections.singletonList(event1));
		InventoryUpdateFunction fun = new InventoryUpdateFunction(restTemplate);

		Tuple2<Flux<ProcessedOrderEvent>, Flux<ProcessedOrderEvent>> processed = fun.apply(orders);
		Flux<ProcessedOrderEvent> fulfillments = processed.getT1();
		Flux<ProcessedOrderEvent> backorders = processed.getT2();

		StepVerifier.create(fulfillments)
				.expectNextCount(1l)
				.assertNext(fulfilled -> {
					assertEquals("homer", fulfilled.getUser());
					assertEquals(1, fulfilled.getProducts().size());
					assertEquals(Integer.valueOf(3), fulfilled.getProducts().get("widget"));
				});
		StepVerifier.create(backorders)
				.expectNextCount(1l)
				.assertNext(backordered -> {
					assertEquals("homer", backordered.getUser());
					assertEquals(2, backordered.getProducts().size());
					assertEquals(Integer.valueOf(7), backordered.getProducts().get("gadget"));
				});
	}
}
