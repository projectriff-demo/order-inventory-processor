package io.projectriff.orderprocessor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderInventoryConfiguration {

	@Bean
	InventoryUpdateFunction inventoryUpdate() {
		return new InventoryUpdateFunction();
	}

}
