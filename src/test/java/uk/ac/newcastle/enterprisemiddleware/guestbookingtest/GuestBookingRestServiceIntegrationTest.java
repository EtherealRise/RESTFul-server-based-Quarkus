package uk.ac.newcastle.enterprisemiddleware.guestbookingtest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

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
import uk.ac.newcastle.enterprisemiddleware.guestbooking.GuestBooking;
import uk.ac.newcastle.enterprisemiddleware.guestbooking.GuestBookingRestService;

@QuarkusTest
@TestHTTPEndpoint(GuestBookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class GuestBookingRestServiceIntegrationTest {

	private static Customer customer = new Customer();
	private static GuestBooking gbooking = new GuestBooking();
	private static Date d = new Date();

	@BeforeAll
	static void setup() {
		customer.setEmail("guest@email.com");
		customer.setName("guest");
		customer.setPhonenumber("01234567890");
		Flight[] flights = when().get("/flights").then().statusCode(200).extract().body().as(Flight[].class);
		Booking b = new Booking();
		b.setFlight(flights[0]);
		b.setDate(d);
		gbooking.setCustomer(customer);
		gbooking.setBooking(b);
	}

	@Test
	@Order(1)
	public void CreateGuestBooking() {
		given().contentType(ContentType.JSON).body(gbooking).when().post().then().statusCode(201);
	}

	@Test
	@Order(2)
	public void CreateWithInvalidCustomer() {
		customer.setEmail("wrong_format");
		given().contentType(ContentType.JSON).body(gbooking).when().post().then().statusCode(400);
	}
	
	@Test
	@Order(3)
	public void CreateWithInvalidBooking() {
		customer.setEmail("willnotshow@email");
		//this time, with same flight and date, we will be failed.
		given().contentType(ContentType.JSON).body(gbooking).when().post().then().statusCode(409);
	}

}
