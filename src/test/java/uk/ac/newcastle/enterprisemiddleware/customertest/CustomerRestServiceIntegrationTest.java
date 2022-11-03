package uk.ac.newcastle.enterprisemiddleware.customerTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerRestService;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestHTTPEndpoint(CustomerRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class CustomerRestServiceIntegrationTest {

	private static Customer customer;

	// Due to we order the retrieved objects by name, we need an initial which is
	// behind A, or the get test might be fail since the object index of the return
	// array could be 0 instead of 1
	@BeforeAll
	static void setup() {
		customer = new Customer();
		customer.setName("XYZ");
		customer.setEmail("customer@email.com");
		customer.setPhonenumber("01234567891");
	}

	@Test
	@Order(1)
	public void testCanCreateCustomer() {
		given().contentType(ContentType.JSON).body(customer).when().post().then().statusCode(201);
	}

	@Test
	@Order(2)
	public void testCanGetCustomers() {
		Response response = when().get().then().statusCode(200).extract().response();

		Customer[] result = response.body().as(Customer[].class);

		System.out.println(result[0]);

		assertEquals(2, result.length);
		assertTrue(customer.getName().equals(result[1].getName()), "Name not equal");
		assertTrue(customer.getEmail().equals(result[1].getEmail()), "Email not equal");
		assertTrue(customer.getPhonenumber().equals(result[1].getPhonenumber()), "Phone number not equal");
	}

	@Test
	@Order(3)
	public void testDuplicateEmailCausesError() {
		given().contentType(ContentType.JSON).body(customer).when().post().then().statusCode(409).body("reasons.email",
				containsString("please use a unique email"));
	}

	@Test
	@Order(4)
	public void testCanDeleteCustomer() {
		Response response = when().get().then().statusCode(200).extract().response();

		Customer[] result = response.body().as(Customer[].class);

		when().delete(result[0].getId().toString()).then().statusCode(204);
	}
}