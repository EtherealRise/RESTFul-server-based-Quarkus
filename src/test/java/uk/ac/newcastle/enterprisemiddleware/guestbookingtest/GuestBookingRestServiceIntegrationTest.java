package uk.ac.newcastle.enterprisemiddleware.guestbookingTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;
import uk.ac.newcastle.enterprisemiddleware.guestbooking.GuestBooking;
import uk.ac.newcastle.enterprisemiddleware.guestbooking.GuestBookingRestService;

import org.junit.jupiter.api.*;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(GuestBookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class GuestBookingRestServiceIntegrationTest {

	private static GuestBooking gbooking;

	@BeforeAll
	static void setup() {
		Customer c = new Customer();
		c.setEmail("guest@email.com");
		c.setName("guest");
		c.setPhonenumber("01234567890");
		Response response = when().get("/flights").then().statusCode(200).extract().response();
		Flight[] flights = response.body().as(Flight[].class);
		Booking b = new Booking();
		b.setCustomer(c);
		b.setFlight(flights[0]);
		b.setDate(new Date());
		gbooking = new GuestBooking();
		gbooking.setCustomer(c);
		gbooking.setBooking(b);
	}

	@Test
	@Order(1)
	public void testCanCreatGuestBooking() throws Exception {

		given().contentType(ContentType.JSON).body(gbooking).when().post().then().statusCode(201);

	}

}