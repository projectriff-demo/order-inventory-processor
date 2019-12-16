package io.projectriff.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArticleQuantityUpdate {

	private final int currentQuantity;

	private final int newQuantity;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public ArticleQuantityUpdate(@JsonProperty("currentQuantity") int currentQuantity,
								 @JsonProperty("newQuantity") int newQuantity) {
		this.currentQuantity = currentQuantity;
		this.newQuantity = newQuantity;
	}

	public int getCurrentQuantity() {
		return currentQuantity;
	}

	public int getNewQuantity() {
		return newQuantity;
	}

	@Override
	public String toString() {
		return "ArticleQuantity{" +
				"currentQuantity=" + currentQuantity +
				", newQuantity=" + newQuantity +
				'}';
	}
}
