package uk.ac.newcastle.enterprisemiddleware.flighttest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;
import uk.ac.newcastle.enterprisemiddleware.flight.FlightRestService;

@QuarkusTest
@TestHTTPEndpoint(FlightRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class FlightRestServiceIntegrationTest {

	private static Flight flight;

	@BeforeAll
	static void setup() {
		flight = new Flight();
		flight.setNumber("A4321");
		flight.setDeparture("ABC");
		flight.setDestination("XYZ");
	}

	@Test
	@Order(1)
	public void CreateFlight() {
		given().contentType(ContentType.JSON).body(flight).when().post().then().statusCode(201);
	}

	@Test
	@Order(2)
	public void GetFlight() {
		Flight[] flights = when().get().then().statusCode(200).extract().response().body().as(Flight[].class);

		assertEquals(2, flights.length);
		assertTrue(flight.getNumber().equals(flights[1].getNumber()), "Number not equal");
		assertTrue(flight.getDeparture().equals(flights[1].getDeparture()), "Departure not equal");
		assertTrue(flight.getDestination().equals(flights[1].getDestination()), "Destination not equal");
	}

	@Test
	@Order(3)
	public void CreateWithDuplicatNumberCausesError() {
		given().contentType(ContentType.JSON).body(flight).when().post().then().statusCode(409).body("reasons.number",
				containsString("please use a unique number"));
	}

	@Test
	@Order(4)
	public void CreateWithEqualDepAndDesCausesError() {
		flight.setNumber("A5678");
		flight.setDeparture("ABC");
		flight.setDestination("ABC");
		given().contentType(ContentType.JSON).body(flight).when().post().then().statusCode(400);
	}
	
	@Test
	@Order(5)
	public void UpdateFlight() {
		Flight[] flights = when().get().then().statusCode(200).extract().response().body().as(Flight[].class);
		assertEquals(2, flights.length);
		flights[1].setDestination("BCD");
		Flight c = given().contentType(ContentType.JSON).body(flights[1])
				.put("/id/{id:.+}", flights[0].getId().toString()).then().statusCode(200).extract()
				.as(Flight.class);
		assertTrue(c.getDestination().equals(flights[1].getDestination()), "Name has not been updated");
	}

	@Test
	@Order(6)
	public void UpdateFlightWithNonexistenceId() {
		flight.setDestination("XYZ");
		given().contentType(ContentType.JSON).body(flight).when().put("/id/{id:.+}", 987654321).then()
				.statusCode(404);
	}

	@Test
	@Order(7)
	public void DeleteFlight() {
		Flight[] flights = when().get().then().statusCode(200).extract().response().body().as(Flight[].class);

		assertEquals(2, flights.length);
		when().delete("/id/{id:.+}", flights[1].getId().toString()).then().statusCode(200);
	}

	@Test
	@Order(8)
	public void GetNonexistenceId() {
		when().get("/id/{id:.+}", 987654321).then().statusCode(404);
	}

	@Test
	@Order(9)
	public void GetWrongIdFormat() {
		when().get("/id/{id:.+}", -1).then().statusCode(404);
	}

	@Test
	@Order(10)
	public void GetNonexistenceNumber() {
		when().get("/number/{number:.+}", "NEXIT").then().statusCode(404);
	}
}
