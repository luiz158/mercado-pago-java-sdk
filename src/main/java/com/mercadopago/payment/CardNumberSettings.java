package com.mercadopago.payment;

import javax.xml.bind.annotation.XmlElement;

public class CardNumberSettings {

	@XmlElement(name = "length")
	private String length;

	@XmlElement(name = "validation")
	private String validation;

	public String getLength() {
		return length;
	}

	public String getValidation() {
		return validation;
	}

	@Override
	public String toString() {
		return "CardNumberSettings [length=" + length + ", validation=" + validation + "]";
	}

}
