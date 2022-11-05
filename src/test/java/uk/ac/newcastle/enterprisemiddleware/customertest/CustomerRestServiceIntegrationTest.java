package uk.ac.newcastle.enterprisemiddleware.customertest;

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
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerRestService;

@QuarkusTest
@TestHTTPEndpoint(CustomerRestService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class CustomerRestServiceIntegrationTest {

	private static Customer customer;

	@BeforeAll
	static void setup() {
		customer = new Customer();
		customer.setName("test");
		customer.setEmail("test@email.com");
		customer.setPhonenumber("01234567890");
	}

	@Test
	@Order(1)
	public void CreateCustomer() {
		given().contentType(ContentType.JSON).body(customer).when().post().then().statusCode(201);
	}

	@Test
	@Order(2)
	public void GetCustomer() {
		Customer[] customers = when().get().then().statusCode(200).extract().response().body().as(Customer[].class);

		assertEquals(2, customers.length);
		assertTrue(customer.getName().equals(customers[1].getName()), "Name not equal");
		assertTrue(customer.getPhonenumber().equals(customers[1].getPhonenumber()), "Phone number not equal");
		assertTrue(customer.getEmail().equals(customers[1].getEmail()), "Email not equal");
	}

	@Test
	@Order(3)
	public void CreateWithDuplicateEmailCausesError() {
		given().contentType(ContentType.JSON).body(customer).when().post().then().statusCode(409).body("reasons.email",
				containsString("please use a unique email"));
	}

	@Test
	@Order(4)
	public void UpdateCustomer() {
		Customer[] customers = when().get().then().statusCode(200).extract().response().body().as(Customer[].class);
		assertEquals(2, customers.length);
		customers[1].setName("rewrite");
		Customer c = given().contentType(ContentType.JSON).body(customers[1])
				.put("/id/{id:.+}", customers[1].getId().toString()).then().statusCode(200).extract()
				.as(Customer.class);
		assertTrue(c.getName().equals(customers[1].getName()), "Name has not been updated");
	}

	@Test
	@Order(5)
	public void UpdateCustomerWithNonexistenceId() {
		given().contentType(ContentType.JSON).body(customer).when().put("/id/{id:.+}", 987654321).then()
				.statusCode(404);
	}

	@Test
	@Order(6)
	public void DeleteCustomer() {
		Customer[] customers = when().get().then().statusCode(200).extract().response().body().as(Customer[].class);

		assertEquals(2, customers.length);
		when().delete("/id/{id:.+}", customers[1].getId().toString()).then().statusCode(200);
	}

	@Test
	@Order(7)
	public void GetNonexistenceId() {
		when().get("/id/{id:.+}", 987654321).then().statusCode(404);
	}

	@Test
	@Order(8)
	public void GetWrongIdFormat() {
		when().get("/id/{id:.+}", -1).then().statusCode(404);
	}

	@Test
	@Order(9)
	public void GetNonexistenceEmail() {
		when().get("/email/{email:.+}", "non-exist@emial.com").then().statusCode(404);
	}

	@Test
	@Order(10)
	public void GetWrongEmailFormat() {
		when().get("/email/{email:.+}", "notemail").then().statusCode(404);
	}

}