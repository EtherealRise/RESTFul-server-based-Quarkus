package uk.ac.newcastle.enterprisemiddleware.bookingtest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.restassured.response.Response;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingRestService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;

@QuarkusTest
@TestHTTPEndpoint(BookingRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class BookingRestServiceIntegrationTest {

	private static Booking booking = new Booking();

	@BeforeAll
	static void setup() {
		Customer[] customers = when().get("/customers").then().statusCode(200).extract().body().as(Customer[].class);
		Flight[] flights = when().get("/flights").then().statusCode(200).extract().body().as(Flight[].class);
		booking.setCustomer(customers[0]);
		booking.setFlight(flights[0]);
		booking.setDate(new Date());
	}

	@Test
	@Order(1)
	public void CreatGuestBooking() {
		Customer c = new Customer();
		c.setId(1);
		c.setName("fake");
		c.setEmail("fake@email.com");
		c.setPhonenumber("01234567890");
		Flight f = new Flight();
		f.setId(1);
		f.setNumber("AFAKE");
		f.setDeparture("ABC");
		f.setDestination("XYZ");
		Booking b = new Booking();
		b.setCustomer(c);
		b.setFlight(f);
		b.setDate(new Date());
		given().contentType(ContentType.JSON).body(b).when().post().then().statusCode(400);

	}

	@Test
	@Order(2)
	public void Createbooking() {
		given().contentType(ContentType.JSON).body(booking).when().post().then().statusCode(201);
	}

	@Test
	@Order(3)
	public void Getbookings() {

		Booking[] bookings = when().get().then().statusCode(200).extract().body().as(Booking[].class);

		assertEquals(1, bookings.length);
		assertTrue(booking.getCustomer().equals(bookings[0].getCustomer()), "Customer not equal");
		assertTrue(booking.getFlight().equals(bookings[0].getFlight()), "Flight not equal");
		assertTrue(booking.getDate().equals(bookings[0].getDate()), "Date not equal");
	}

	@Test
	@Order(4)
	public void Deletebooking() {
		Response response = when().get().then().statusCode(200).extract().response();

		Booking[] result = response.body().as(Booking[].class);

		when().delete(result[0].getId().toString()).then().statusCode(200);
	}

	@Test
	@Order(5)
	public void GetBookingMadeByOneCustomer() {
		Createbooking();
		Booking[] bookings = when().get("http://localhost:8080/customers/booking/1").then().statusCode(200).extract()
				.response().body().as(Booking[].class);

		assertEquals(1, bookings.length);
		assertTrue(booking.getCustomer().equals(bookings[0].getCustomer()), "Customer not equal");
		assertTrue(booking.getFlight().equals(bookings[0].getFlight()), "Flight not equal");
		assertTrue(booking.getDate().equals(bookings[0].getDate()), "Date not equal");
	}

	@Test
	@Order(6)
	public void DeletebookingAndCheckByCustomer() {
		Deletebooking();
		Booking[] bookings = when().get("http://localhost:8080/customers/booking/1").then().statusCode(200).extract()
				.response().body().as(Booking[].class);
		assertEquals(0, bookings.length);
	}

	@Test
	@Order(7)
	public void DeleteCustomerAndCheckBooking() {
		Createbooking();
		when().delete("http://localhost:8080/customers/id/1").then().statusCode(200);
		Booking[] bookings = when().get().then().statusCode(200).extract().body().as(Booking[].class);
		assertEquals(0, bookings.length);
	}

}
