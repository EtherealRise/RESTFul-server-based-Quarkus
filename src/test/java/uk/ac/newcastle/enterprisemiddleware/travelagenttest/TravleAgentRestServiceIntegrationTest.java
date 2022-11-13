package uk.ac.newcastle.enterprisemiddleware.travelagenttest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;
import uk.ac.newcastle.enterprisemiddleware.travelagentbooking.TABooking;
import uk.ac.newcastle.enterprisemiddleware.travelagentbooking.TABookingRestService;

@QuarkusTest
@TestHTTPEndpoint(TABookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class TravleAgentRestServiceIntegrationTest {

	private static Booking flightbooking = new Booking();
	private static TABooking tabooking = new TABooking();

	@BeforeAll
	static void setup() {
		Customer[] customers = when().get("/customers").then().statusCode(200).extract().body().as(Customer[].class);
		Flight[] flights = when().get("/flights").then().statusCode(200).extract().body().as(Flight[].class);
		assertEquals(1, customers.length);
		assertEquals(1, flights.length);
		flightbooking.setCustomer(customers[0]);
		flightbooking.setFlight(flights[0]);
		flightbooking.setDate(new Date());
		tabooking.setCustomer(customers[0]);
		tabooking.setFlightbooking(flightbooking);
	}

	@Test
	@Order(1)
	public void CreateTravleAgentBooking() {
		given().contentType(ContentType.JSON).body(tabooking).when().post().then().statusCode(201);
	}

	@Test
	@Order(2)
	public void GetTravleAgentBookingMadeByOneCustomer() {
		// to keep simplify, let's use the same customer and check his flight booking
		// firstly

		Booking[] booking = when().get("http://localhost:8080/customers/booking/1").then().statusCode(200).extract()
				.response().body().as(Booking[].class);

		assertEquals(3, booking.length);
		// little hard code here since we know the flight record has been created first
		assertTrue(flightbooking.getCustomer().equals(booking[0].getCustomer()), "Customer not equal");
		assertTrue(flightbooking.getFlight().equals(booking[0].getFlight()), "Flight not equal");
		assertTrue(flightbooking.getDate().equals(booking[0].getDate()), "Date not equal");

		// now we retrieve the travel agent booking belong to this customer
		TABooking[] tabookings = when().get("http://localhost:8080/customers/tabooking/1").then().statusCode(200)
				.extract().response().body().as(TABooking[].class);

		assertEquals(1, tabookings.length);
		assertTrue(flightbooking.getCustomer().equals(tabookings[0].getCustomer()), "Customer not equal");
		assertTrue(flightbooking.getFlight().equals(tabookings[0].getFlightbooking().getFlight()), "Flight not equal");
		assertTrue(flightbooking.getDate().equals(tabookings[0].getFlightbooking().getDate()), "Date not equal");
	}

	@Test
	@Order(3)
	public void InvalidBookingChangeNothing() {
		// Due to we are duplicating flight service three times, this test had be done
		// manually
		// step1. inject a invalid hotel booking in TravleAngentRESTserivce
		// step2. check whether database had clean recorded flight/taxi booking
		// step3. confirm the database is consistent
	}

}
