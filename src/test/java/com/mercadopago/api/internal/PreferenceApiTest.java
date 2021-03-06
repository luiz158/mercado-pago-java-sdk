package com.mercadopago.api.internal;


import static com.mercadopago.api.paymentmethod.PaymentType.TICKET;
import static com.mercadopago.api.preference.Preference.PreferenceOperationType.REGULAR_PAYMENT;
import static com.mercadopago.api.preference.Shipment.Mode.CUSTOM;
import static com.mercadopago.api.preference.Shipment.Mode.NOT_SPECIFIED;
import static java.math.BigDecimal.TEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mercadopago.api.exception.MercadoPagoBadRequestException;
import com.mercadopago.api.oauth.MercadoPagoToken;
import com.mercadopago.api.paymentmethod.ExcludedPaymentType;
import com.mercadopago.api.paymentmethod.PaymentMethod;
import com.mercadopago.api.preference.Address;
import com.mercadopago.api.preference.Item;
import com.mercadopago.api.preference.Phone;
import com.mercadopago.api.preference.Preference;
import com.mercadopago.api.preference.PreferencePayer;
import com.mercadopago.api.preference.PreferencePaymentMethods;
import com.mercadopago.api.preference.ReceiverAddress;
import com.mercadopago.api.preference.Shipment;
import com.mercadopago.api.preference.Shipment.Mode;
import com.mercadopago.api.token.MercadoPagoCredentials;
import com.mercadopago.api.token.PropertiesReader;

public class PreferenceApiTest {

	private static MercadoPagoApi mercadoPagoApi;

	@BeforeClass
	public static void generateToken() {
		String clientId = new PropertiesReader().getPropertyValueFrom(MercadoPagoToken.CLIENT_ID);
		String secretKey = new PropertiesReader().getPropertyValueFrom(MercadoPagoToken.SECRET_KEY);
		MercadoPagoCredentials credentials = new MercadoPagoCredentials(clientId, secretKey);
		MercadoPagoToken token = MercadoPagoApiFactory.generateProductionTokenUsing(credentials);
		
		mercadoPagoApi = MercadoPagoApiFactory.enableApiOperationsFrom(token);
	}
	
	@Test
	public void shouldCreateANewPreferenceWithAllInformations() throws Exception {
		Preference preference = new Preference();
		Item item = Item
			.fromId("1")
			.withProductNamed("First Product")
			.withDescription("First Awesome Product")
			.costing(TEN)
			.withQuantity(10)
			.usingPictureOnUrl("http://s3.amazon.com/mercadopago/image.png")
			.fromCategory("Music")
			.withCurrecyCode("ARS")
			.build();
		
		PreferencePayer payer = new PreferencePayer();
		
		payer.setName("Alexandre");
		payer.setLastname("Gama");
		payer.setEmail("alexandre.gama.lima@gmail.com");
		payer.setPhone(new Phone("55", "987653786"));
		
		Address address = new Address();
		address.setZipCode("04676500");
		address.setStreetNumber(40);
		address.setStreetName("First Street");
		payer.setAddress(address);
		
		preference.addItem(item);
		preference.setAdditionalInformation("Elo7 - Additional Infos");
		preference.setPayer(payer);
		
		Preference preferenceCreted = mercadoPagoApi.preferences().createNew(preference);
		Item cretedItem = preferenceCreted.getItems().get(0);
		
		assertThat(cretedItem.getId(), is(equalTo("1")));
		assertThat(cretedItem.getTitle(), is(equalTo("First Product")));
		assertThat(cretedItem.getDescription(), is(equalTo("First Awesome Product")));
		assertThat(cretedItem.getPrice(), is(equalTo(BigDecimal.TEN)));
		assertThat(cretedItem.getQuantity(), is(equalTo(10)));
		assertThat(cretedItem.getPictureUrl(), is(equalTo("http://s3.amazon.com/mercadopago/image.png")));
		assertThat(cretedItem.getCategory(), is(equalTo("Music")));
		assertThat(cretedItem.getCurrency(), is(equalTo("ARS")));
		
		assertThat(preferenceCreted.getId(), is(notNullValue()));
		assertThat(preferenceCreted.getCollectorId(), is(notNullValue()));
		assertThat(preferenceCreted.getOperationType(), is(equalTo(REGULAR_PAYMENT)));
		assertThat(preferenceCreted.getAdditionalInformation(), is(equalTo("Elo7 - Additional Infos")));
		
		assertThat(preferenceCreted.getBackUrl().getSuccess(), is(notNullValue()));
		assertThat(preferenceCreted.getBackUrl().getPending(), is(notNullValue()));
		assertThat(preferenceCreted.getBackUrl().getFailure(), is(notNullValue()));
		
		PreferencePayer payerFromPreference = preferenceCreted.getPayer();
		assertThat(payerFromPreference.getName(), is(equalTo("Alexandre")));
		assertThat(payerFromPreference.getLastname(), is(equalTo("Gama")));
		assertThat(payerFromPreference.getEmail(), is(equalTo("alexandre.gama.lima@gmail.com")));
		assertThat(payerFromPreference.getPhone().getAreaCode(), is(equalTo("55")));
		assertThat(payerFromPreference.getPhone().getNumber(), is(equalTo("987653786")));
		assertThat(payerFromPreference.getAddress().getStreetName(), is(equalTo("First Street")));
		assertThat(payerFromPreference.getAddress().getStreetNumber(), is(equalTo(40)));
		assertThat(payerFromPreference.getAddress().getZipCode(), is(equalTo("04676500")));
		
	}
	
	@Test
	public void shouldCreateANewPreferenceWithPreferenceId() throws Exception {
		Preference preference = new Preference();
		preference.setId("1");
		Item item = Item
				.fromId("1")
				.withProductNamed("First Produto")
				.withDescription("First Awesome Product")
				.costing(TEN)
				.withQuantity(10)
				.usingPictureOnUrl("http://s3.amazon.com/mercadopago/image.png")
				.fromCategory("Music")
				.withCurrecyCode("BRL")
				.build();
		
		preference.addItem(item);
		
		Preference preferenceCreted = mercadoPagoApi.preferences().createNew(preference);
		Item cretedItem = preferenceCreted.getItems().get(0);
		
		assertThat(preferenceCreted.getId(), is(notNullValue()));
		assertThat(cretedItem.getId(), is(equalTo("1")));
	}

	@Test(expected = MercadoPagoBadRequestException.class)
	public void shouldNotCreateANewPreferenceWhenUserDoesNotSendProductPrice() throws Exception {
		Preference preference = new Preference();
		Item item = new Item();
		item.setId("1");
		item.setQuantity(3);
		item.setCategory("Music");
		item.setCurrency("BRL");
		
		preference.addItem(item);
		mercadoPagoApi.preferences().createNew(preference);
	}
	
	@Test(expected = MercadoPagoBadRequestException.class)
	public void shouldNotCreateANewPreferenceWhenUserDoesNotSendQuantity() throws Exception {
		Preference preference = new Preference();
		Item item = new Item();
		item.setId("1");
		item.setPrice(TEN);
		item.setCategory("Music");
		item.setCurrency("BRL");
		
		preference.addItem(item);
		mercadoPagoApi.preferences().createNew(preference);
	}
	
	@Test(expected = MercadoPagoBadRequestException.class)
	public void shouldNotCreateANewPreferenceWhenDoesNotSendAtLeastOneItem() throws Exception {
		Preference preference = new Preference();
		Item item = new Item();
		item.setId("1");
		item.setPrice(TEN);
		item.setCategory("Music");
		item.setCurrency("BRL");
		
		mercadoPagoApi.preferences().createNew(preference);
	}
	
	@Test
	public void shouldCreateANewPreferenceWhenUserSendAllMinimalInformations() throws Exception {
		Preference preference = new Preference();
		Item item = new Item();
		item.setPrice(BigDecimal.TEN);
		item.setQuantity(3);
		
		preference.addItem(item);
		mercadoPagoApi.preferences().createNew(preference);
	}

	@Test
	public void shouldCreateANewPreferenceWithExcludedPaymentMethod() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();
		
		PaymentMethod paymentMethodToBeExcluded = mercadoPagoApi.paymentMethods().findBy("visa").get();
		PreferencePaymentMethods excludedPaymentMethods = new PreferencePaymentMethods();
		excludedPaymentMethods.addPaymentMethodToBeExcluded(paymentMethodToBeExcluded);
		
		ExcludedPaymentType paymentTypeToBeExcluded = new ExcludedPaymentType();
		paymentTypeToBeExcluded.setPaymentType(TICKET);
		excludedPaymentMethods.addPaymentTypeToBeExcluded(paymentTypeToBeExcluded);
		
		preference.addItem(item);
		preference.setPaymentMethods(excludedPaymentMethods);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		PreferencePaymentMethods paymentMethod = preferenceCreated.getPaymentMethods();
		
		paymentMethod.getExcludedPaymentMethods().forEach(method -> assertThat(method.getId(), is(equalTo("visa"))));
		paymentMethod.getPaymentTypes().forEach(type -> assertThat(type.getPaymentType(), is(equalTo(TICKET.getName()))));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithDefaultPaymentMethod() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();
		
		PaymentMethod paymentMethodToBeDefault = mercadoPagoApi.paymentMethods().findBy("visa").get();
		
		PreferencePaymentMethods paymentMethods = new PreferencePaymentMethods();
		paymentMethods.setDefaultPaymentMethod(paymentMethodToBeDefault.getId());
		
		preference.addItem(item);
		preference.setPaymentMethods(paymentMethods);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		String defaultPaymentMethodId = preferenceCreated.getPaymentMethods().getDefaultPaymentMethod();
		
		assertThat(defaultPaymentMethodId, is(equalTo(paymentMethodToBeDefault.getId())));
	}
	
	@Test
	public void shouldCreateANewPreferenceSettingMaximumInstallmentsAllowed() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();

		PreferencePaymentMethods paymentMethods = new PreferencePaymentMethods();
		paymentMethods.setMaximumInstallmentsAllowed(12);
		
		preference.addItem(item);
		preference.setPaymentMethods(paymentMethods);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Integer maximumInstallmentsAllowed = preferenceCreated.getPaymentMethods().getMaximumInstallmentsAllowed();
		
		assertThat(maximumInstallmentsAllowed, is(equalTo(12)));
	}

	@Test
	public void shouldCreateANewPreferenceSettingThePreferedNumberOfInstallmentsForCreditCard() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();
		
		PreferencePaymentMethods paymentMethods = new PreferencePaymentMethods();
		paymentMethods.setPreferedInstallmentsForCreditCard(3);
		
		preference.addItem(item);
		preference.setPaymentMethods(paymentMethods);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Integer preferedInstallmentsForCreditCard = preferenceCreated.getPaymentMethods().getPreferedInstallmentsForCreditCard();
		
		assertThat(preferedInstallmentsForCreditCard, is(equalTo(3)));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithShipmentsUsingMode() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();
		
		Shipment shipments = new Shipment();
		shipments.setMode(CUSTOM);
		
		preference.addItem(item);
		preference.setShipments(shipments);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Mode mode = preferenceCreated.getShipments().getMode();
		
		assertThat(mode, is(equalTo(CUSTOM)));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithShipmentsUsingCustomLocalPickup() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();
		
		Shipment shipments = new Shipment();
		shipments.setMode(CUSTOM);
		shipments.notUsingLocalPickup();
		
		preference.addItem(item);
		preference.setShipments(shipments);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Mode mode = preferenceCreated.getShipments().getMode();
		boolean isUsingLocalPickup = preferenceCreated.getShipments().isUsingLocalPickup();
		
		assertThat(mode, is(equalTo(CUSTOM)));
		assertFalse(isUsingLocalPickup);
	}
	
	@Test
	public void shouldCreateANewPreferenceWithShipmentsUsingNotSpecifiedLocalPickup() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(TEN).withQuantity(3).build();
		
		Shipment shipments = new Shipment();
		shipments.setMode(NOT_SPECIFIED);
		shipments.notUsingLocalPickup();
		
		preference.addItem(item);
		preference.setShipments(shipments);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Mode mode = preferenceCreated.getShipments().getMode();
		boolean isUsingLocalPickup = preferenceCreated.getShipments().isUsingLocalPickup();
		
		assertThat(mode, is(equalTo(NOT_SPECIFIED)));
		assertFalse(isUsingLocalPickup);
	}
	
	@Test
	public void shouldCreateANewPreferenceWithShipmentsUsingCostWhenModeIsCustom() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		Shipment shipments = new Shipment();
		shipments.setMode(CUSTOM);
		shipments.setCost(TEN);
		
		preference.addItem(item);
		preference.setShipments(shipments);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		BigDecimal customShippingCost = preferenceCreated.getShipments().getCost();
		
		assertThat(customShippingCost, is(equalTo(TEN)));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithShipmentsUsingFreeShipingForCustomMode() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		Shipment shipments = new Shipment();
		shipments.setMode(CUSTOM);
		shipments.usingFreeShipping();
		
		preference.addItem(item);
		preference.setShipments(shipments);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Boolean usingFreeShipping = preferenceCreated.getShipments().isUsingFreeShipping();
		
		assertTrue(usingFreeShipping);
	}
	
	@Test
	public void shouldCreateANewPreferenceWithShipmentsIsNotUsingFreeShipingForCustomMode() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		Shipment shipments = new Shipment();
		shipments.setMode(CUSTOM);
		shipments.notUsingFreeShipping();
		
		preference.addItem(item);
		preference.setShipments(shipments);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		Boolean usingFreeShipping = preferenceCreated.getShipments().isUsingFreeShipping();
		
		assertFalse(usingFreeShipping);
	}
	
	@Test
	public void shouldCreateANewPreferenceWithZipcodeOnShippingAddress() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		ReceiverAddress address = new ReceiverAddress();
		address.setZipcode("123456789");
		
		Shipment shipment = new Shipment();
		shipment.setReceiverAddress(address);
		
		preference.addItem(item);
		preference.setShipments(shipment);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		ReceiverAddress receiverAddress = preferenceCreated.getShipments().getReceiverAddress();
		
		assertThat(receiverAddress.getZipcode(), is(equalTo("123456789")));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithStreetNameOnShippingAddress() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		ReceiverAddress address = new ReceiverAddress();
		address.setStreetName("Rua Beira Rio");
		
		Shipment shipment = new Shipment();
		shipment.setReceiverAddress(address);
		
		preference.addItem(item);
		preference.setShipments(shipment);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		ReceiverAddress receiverAddress = preferenceCreated.getShipments().getReceiverAddress();
		
		assertThat(receiverAddress.getStreetName(), is(equalTo("Rua Beira Rio")));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithStreetNumberOnShippingAddress() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		ReceiverAddress address = new ReceiverAddress();
		address.setStreetNumber(158);
		
		Shipment shipment = new Shipment();
		shipment.setReceiverAddress(address);
		
		preference.addItem(item);
		preference.setShipments(shipment);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		ReceiverAddress receiverAddress = preferenceCreated.getShipments().getReceiverAddress();
		
		assertThat(receiverAddress.getStreetNumber(), is(equalTo(158)));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithFloorOnShippingAddress() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		ReceiverAddress address = new ReceiverAddress();
		address.setFloor("Ap 32");
		
		Shipment shipment = new Shipment();
		shipment.setReceiverAddress(address);
		
		preference.addItem(item);
		preference.setShipments(shipment);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		ReceiverAddress receiverAddress = preferenceCreated.getShipments().getReceiverAddress();
		
		assertThat(receiverAddress.getFloor(), is(equalTo("Ap 32")));
	}
	
	@Test
	public void shouldCreateANewPreferenceWithApartmentOnShippingAddress() throws Exception {
		Preference preference = new Preference();
		Item item = Item.fromId("1").costing(BigDecimal.TEN).withQuantity(3).build();
		
		ReceiverAddress address = new ReceiverAddress();
		address.setFloor("Ap 32");
		
		Shipment shipment = new Shipment();
		shipment.setReceiverAddress(address);
		
		preference.addItem(item);
		preference.setShipments(shipment);
		
		Preference preferenceCreated = mercadoPagoApi.preferences().createNew(preference);
		ReceiverAddress receiverAddress = preferenceCreated.getShipments().getReceiverAddress();
		
		assertThat(receiverAddress.getFloor(), is(equalTo("Ap 32")));
	}
	
}
