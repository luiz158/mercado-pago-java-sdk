package com.mercadopago.payment;

import static com.mercadopago.payment.OrderOnPayment.OrderType.MERCADOPAGO;
import static com.mercadopago.payment.Payment.OperationType.REGULAR_PAYMENT;
import static com.mercadopago.token.MercadoPagoTokenGenerator.ENVIRONMENT_MODE.SANDBOX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.mercadopago.api.MercadoPagoJerseyClient;
import com.mercadopago.api.MercadoPagoToken;
import com.mercadopago.api.TokenClientCredentialsReader;
import com.mercadopago.api.TokenCredentials;
import com.mercadopago.paymentmethod.PaymentMethod;
import com.mercadopago.preference.Address;
import com.mercadopago.token.MercadoPagoTokenGenerator;

/**
 * 
 * @author Alexandre Gama
 *
 */
public class PaymentClientApiTest {

	private MercadoPagoJerseyClient mercadoPagoApi;

	@Before
	public void getCredentials() {
		TokenCredentials credentials = new TokenClientCredentialsReader().getCredentialsForFile("config.properties");
		MercadoPagoTokenGenerator tokenGenerator = new MercadoPagoTokenGenerator();
		MercadoPagoToken token = tokenGenerator.generateUsing(credentials, SANDBOX);
		
		mercadoPagoApi = new MercadoPagoJerseyClient(token);
	}
	
	@Test
	public void shouldCreateANewPaymentWithAllRequiredFields() throws Exception {
		PaymentMethod paymentMethod = mercadoPagoApi.paymentMethods().getBy("pagofacil").get();
		
		PaymentPayer payer = new PaymentPayer();
		payer.setCustomerId("218136417-Npn1qbvt94mMJ2");
		payer.setEmail("alexandre.gama@elo7.com");
		
		Address address = new Address();
		address.setStreetName("Rua Beira Rio");
		address.setStreetNumber(70);
		address.setZipCode("04689115");
		
		PaymentWithRequiredFields payment = new PaymentWithRequiredFields();
		payment.setDescription("Title of what you are paying for");
		payment.setTransactionAmount(BigDecimal.TEN);
		payment.setPaymentMethodId(paymentMethod.getId());
		payment.setInstallments(12);
		payment.setPayer(payer);
		
		Payment paymentCreated = mercadoPagoApi.payments().createNew(payment);
		
		assertThat(paymentCreated.getId(), is(notNullValue()));
		assertThat(paymentCreated.getPaymentMethodId(), is(equalTo("pagofacil")));
		assertThat(paymentCreated.getDescription(), is(equalTo("Title of what you are paying for")));
		assertThat(paymentCreated.getInstallments(), is(equalTo(1)));
	}
	
	@Test
	public void shouldCreateANewPaymentWithAllRequiredFieldsAndReturningOperationType() throws Exception {
		PaymentMethod paymentMethod = mercadoPagoApi.paymentMethods().getBy("pagofacil").get();
		
		PaymentPayer payer = new PaymentPayer();
		payer.setCustomerId("218136417-Npn1qbvt94mMJ2");
		payer.setEmail("alexandre.gama@elo7.com");
		
		Address address = new Address();
		address.setStreetName("Rua Beira Rio");
		address.setStreetNumber(70);
		address.setZipCode("04689115");
		
		PaymentWithRequiredFields payment = new PaymentWithRequiredFields();
		payment.setDescription("Title of what you are paying for");
		payment.setTransactionAmount(BigDecimal.TEN);
		payment.setPaymentMethodId(paymentMethod.getId());
		payment.setInstallments(12);
		payment.setPayer(payer);
		
		Payment paymentCreated = mercadoPagoApi.payments().createNew(payment);
		
		assertThat(paymentCreated.getOperationType(), is(equalTo(REGULAR_PAYMENT)));
	}
	
	@Test
	public void shouldCreateANewPaymentWithAllRequiredFieldsAndOrderIdentifier() throws Exception {
		PaymentMethod paymentMethod = mercadoPagoApi.paymentMethods().getBy("pagofacil").get();
		
		PaymentPayer payer = new PaymentPayer();
		payer.setCustomerId("218136417-Npn1qbvt94mMJ2");
		payer.setEmail("alexandre.gama@elo7.com");
		
		Address address = new Address();
		address.setStreetName("Rua Beira Rio");
		address.setStreetNumber(70);
		address.setZipCode("04689115");
		
		PaymentWithRequiredFields payment = new PaymentWithRequiredFields();
		payment.setDescription("Title of what you are paying for");
		payment.setTransactionAmount(BigDecimal.TEN);
		payment.setPaymentMethodId(paymentMethod.getId());
		payment.setInstallments(12);
		payment.setPayer(payer);
		
		payment.setOrder(new OrderOnPayment(MERCADOPAGO, 10L));
		
		Payment paymentCreated = mercadoPagoApi.payments().createNew(payment);
		OrderOnPayment order = paymentCreated.getOrder();
		
		assertThat(paymentCreated.getOperationType(), is(equalTo(REGULAR_PAYMENT)));
		assertThat(order.getId(), is(equalTo(10L)));
		assertThat(order.getType(), is(equalTo(MERCADOPAGO)));
	}
	
}
