package uk.ac.newcastle.enterprisemiddleware.flightTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;
import uk.ac.newcastle.enterprisemiddleware.flight.FlightRestService;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(FlightRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class FlightRestServiceIntegrationTest {

    private static Flight flight;

    @BeforeAll
    static void setup() throws Exception{
        flight = new Flight();
        flight.setNumber("A5678");
        flight.setDeparture("ABC");
        flight.setDestination("XYZ");
    }

    @Test
    @Order(1)
    public void testCanCreateFlight() {
        given().
                contentType(ContentType.JSON).
                body(flight).
        when()
                .post().
        then().
                statusCode(201);
    }

    @Test
    @Order(2)
    public void testCanGetFlights() {
        Response response = when().
                get().
        then().
                statusCode(200).
                extract().response();

        Flight[] result = response.body().as(Flight[].class);

        System.out.println(result[0]);

        assertEquals(2, result.length);
        assertTrue(flight.getNumber().equals(result[1].getNumber()), "Name not equal");
        assertTrue(flight.getDeparture().equals(result[1].getDeparture()), "Email not equal");
        assertTrue(flight.getDestination().equals(result[1].getDestination()), "Phone number not equal");
    }

    @Test
    @Order(3)
    public void testDuplicateNumberCausesError() {
        given().
                contentType(ContentType.JSON).
                body(flight).
        when().
                post().
        then().
                statusCode(409).
                body("reasons.number", containsString("number is already used"));
    }

    @Test
    @Order(4)
    public void testCanDeleteFlight() {
        Response response = when().
                get().
                then().
                statusCode(200).
                extract().response();

        Flight[] result = response.body().as(Flight[].class);

        when().
                delete(result[0].getId().toString()).
        then().
                statusCode(204);
    }
}