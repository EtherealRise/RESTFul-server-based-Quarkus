package uk.ac.newcastle.enterprisemiddleware.bookingTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingRestService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;

import org.junit.jupiter.api.*;
import java.util.Date;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(BookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class BookingRestServiceIntegrationTest {

	private static Booking booking;

	@BeforeAll
	static void setup() {
		Response response = when().get("/customers").then().statusCode(200).extract().response();
		Customer[] customers = response.body().as(Customer[].class);
		System.out.println(customers[0].getEmail());
		response = when().get("/flights").then().statusCode(200).extract().response();
		Flight[] flights = response.body().as(Flight[].class);
		System.out.println(flights[0].getNumber());
		booking = new Booking();
		booking.setCustomer(customers[0]);
		booking.setFlight(flights[0]);
		booking.setDate(new Date());
	}

	@Test
	@Order(1)
	public void testCanCreatGuestBooking() throws Exception {
		Customer c = new Customer();
		c.setId(1L);
		c.setEmail("fake@email.com");
		c.setPhonenumber("01234567890");
		c.setName("adsfadf");
		Flight f = new Flight();
		// we must provide fake info or jackson mapper will fail due to flight requirement doesn't meet
		// same for customer
		f.setId(1L);
		f.setDeparture("BSD");
		f.setDestination("BSX");
		f.setNumber("A5565");
		Booking b = new Booking();
		System.out.print("what's the email");
		System.out.print(c.getEmail());
		b.setCustomer(c);
		b.setFlight(f);
		b.setDate(new Date());
		System.out.print(c.getEmail());
		System.out.print(f.getNumber());
		System.out.print("start post!");
		given().contentType(ContentType.JSON).body(b).when().post().then().statusCode(400).body("reasons.customer",
				containsString("The customer " + b.getCustomer().getEmail() + " is not exist"));;

	}

//	@Test
//	@Order(2)
//	public void testCanCreatebooking() {
//		given().contentType(ContentType.JSON).body(booking).when().post().then().statusCode(201);
//	}
//
//	@Test
//	@Order(3)
//	public void testCanGetbookings() {
//		Response response = when().get().then().statusCode(200).extract().response();
//
//		Booking[] result = response.body().as(Booking[].class);
//
//		System.out.println(result[0]);
//
//		assertEquals(1, result.length);
//		assertTrue(booking.getCustomer().equals(result[0].getCustomer()), "Customer not equal");
//		assertTrue(booking.getFlight().equals(result[0].getFlight()), "Flight not equal");
//		assertTrue(booking.getDate().equals(result[0].getDate()), "Date not equal");
//	}
//
//	@Test
//	@Order(4)
//	public void testDuplicateEmailCausesError() {
//		given().contentType(ContentType.JSON).body(booking).when().post().then().statusCode(409).body("reasons.flight",
//				containsString("The flight" + booking.getFlight().getNumber() + "is already booked"));
//	}
//
//	@Test
//	@Order(5)
//	public void testCanDeletebooking() {
//		Response response = when().get().then().statusCode(200).extract().response();
//
//		Booking[] result = response.body().as(Booking[].class);
//
//		when().delete(result[0].getId().toString()).then().statusCode(204);
//	}
}