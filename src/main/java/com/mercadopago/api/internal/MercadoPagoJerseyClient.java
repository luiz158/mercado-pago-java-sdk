package com.mercadopago.api.internal;

import com.google.common.base.MoreObjects;
import com.mercadopago.api.service.JerseyPaymentApi;
import com.mercadopago.api.service.PaymentMethodApi;
import com.mercadopago.api.service.PreferenceApi;
import com.mercadopago.token.MercadoPagoToken;

public class MercadoPagoJerseyClient implements MercadoPagoApi {

	private final MercadoPagoToken token;
	
	public MercadoPagoJerseyClient(final MercadoPagoToken token) {
		this.token = token;
	}

	public PaymentMethodApi paymentMethods() {
		return new PaymentMethodApi(token);
	}

	public PreferenceApi preferences() {
		return new PreferenceApi(token);
	}
	
	@Override
	public JerseyPaymentApi payments() {
		return new JerseyPaymentApi(token);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("token", token)
		.toString();
	}

	
}
