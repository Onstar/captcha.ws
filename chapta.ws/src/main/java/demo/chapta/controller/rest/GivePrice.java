package demo.chapta.controller.rest;

import demo.chapta.model.Point;


public class GivePrice {
	
	public Point getPrice() {
		return price;
	}

	public void setPrice(Point price) {
		this.price = price;
	}

	public Point getInputBox() {
		return inputBox;
	}

	public void setInputBox(Point inputBox) {
		this.inputBox = inputBox;
	}

	public Point getButton() {
		return button;
	}

	public void setButton(Point button) {
		this.button = button;
	}

	private Point price, inputBox, button;

}