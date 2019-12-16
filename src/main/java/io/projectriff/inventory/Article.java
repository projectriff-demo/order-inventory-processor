package io.projectriff.inventory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Article {

  private final String sku;

  private final String name;

  private final String description;

  private final BigDecimal priceInUsd;

  private final String imageUrl;

  private final int quantity;

  public Map<String, Object> links;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Article(@JsonProperty("sku") String sku,
                 @JsonProperty("name") String name,
                 @JsonProperty("description") String description,
                 @JsonProperty("priceInUsd") BigDecimal priceInUsd,
                 @JsonProperty("imageUrl") String imageUrl,
                 @JsonProperty("quantity") int quantity,
                 @JsonProperty("_links") Map<String, Object> links) {
    this.sku = sku;
    this.name = name;
    this.description = description;
    this.priceInUsd = priceInUsd;
    this.imageUrl = imageUrl;
    this.quantity = quantity;
    this.links = links;
  }

  public String getSku() {
    return sku;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPriceInUsd() {
    return priceInUsd;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public int getQuantity() {
    return quantity;
  }

  public Map<String, Object> getLinks() {
    return links;
  }

  public void setLinks(Map<String, Object> links) {
    this.links = links;
  }

  @Override
  public String toString() {
    return "Article{" +
      "sku='" + sku + '\'' +
      ", name='" + name + '\'' +
      ", description='" + description + '\'' +
      ", priceInUsd=" + priceInUsd + '\'' +
      ", imageUrl=" + imageUrl + '\'' +
      ", quantity=" + quantity +
      ", _links=" + links +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Article article = (Article) o;
    return Objects.equals(sku, article.sku) &&
      Objects.equals(name, article.name) &&
      Objects.equals(description, article.description) &&
      Objects.equals(priceInUsd, article.priceInUsd) &&
      Objects.equals(imageUrl, article.imageUrl) &&
      Objects.equals(quantity, article.quantity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sku, name, description, priceInUsd, imageUrl, quantity);
  }
}
