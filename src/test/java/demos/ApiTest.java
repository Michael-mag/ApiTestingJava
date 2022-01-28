package demos;

import models.Product;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApiTest {

    @Test
    public void getCategories() {
        String endPoint = "http://localhost:8888/api_testing/category/read.php";
        var response = given().when().get(endPoint).then();
        response.log().body();
    }

    @Test
    public void getProduct() {
        String endPoint = "http://localhost:8888/api_testing/product/read_one.php";
        given().
            queryParam("id", 2).
        when().
            get(endPoint).
        then().
            assertThat().statusCode(200).
                body("id", equalTo("2")).
                body("name", equalTo("Cross-Back Training Tank")).
                body("description", equalTo("The most awesome phone of 2013!")).
                body("price", equalTo("299.00")).
                body("category_id", equalTo("2"));
    }

    // A more general test for everything in the response
    @Test
    public void getProducts() {
        String endPoint = "http://localhost:8888/api_testing/product/read.php";
        given().
                queryParam("id", 2).
                when().
                get(endPoint).
                then().
                assertThat().statusCode(200).
                    header("Content-Type", equalTo("application/json; charset=UTF-8")).
                    body("records.size()", greaterThan(0)).
                    body("records.id", everyItem(notNullValue())).
                    body("records.name", everyItem(notNullValue())).
                    body("records.description", everyItem(notNullValue())).
                    body("records.price", everyItem(notNullValue())).
                    body("records.category_name", everyItem(notNullValue()));
    }
    @Test
    public void createProduct() {
        /*
            Basically creating a product requires 4 pieces of information:
                1. Product name
                2. Product description
                3. Product price
                4. Product category id
            Pass these as a payload
        */
        String endPoint = "http://localhost:8888/api_testing/product/create.php";
        String payload = """
                {
                    "name": "Water Bottle",
                    "description": "Blue water bottle holds 1 litre of water",
                    "price": 12,
                    "category_id": 3
                }
                """;
        var response = given().body(payload).when().post(endPoint).then();
        response.log().body();
    }

    @Test
    public void updateProduct() {
        String endPoint = "http://localhost:8888/api_testing/product/update.php";
        String payload = """
                {
                    "id": 1002,
                    "name": "Water Bottle",
                    "description": "Blue water bottle holds 1 litre of water",
                    "price": 25,
                    "category_id": 3
                }
                """;
        var response = given().body(payload).when().put(endPoint).then();
        response.log().body();
    }

    @Test
    public void deleteProduct() {
        String endPoint = "http://localhost:8888/api_testing/product/delete.php";
        String payload = """
                {
                    "id": 1002
                }
                """;
        var response = given().body(payload).when().delete(endPoint).then();
        response.log().body();
    }

    @Test
    public void createSerializedProduct() {
        String endPoint = "http://localhost:8888/api_testing/product/create.php";
        Product product = new Product(
                "Water Bottle",
                "Blue Water Bottle. Holds 1 litre.",
                12,
                3
        );

        var response = given().body(product).when().post(endPoint).then();
        response.log().body();
    }

    // perform a get request and map the response to the Product class
    @Test
    public void getDeserializedProduct() {
        String endPoint = "http://localhost:8888/api_testing/product/read_one.php";

        Product expectedProduct = new Product(
            2,
            "Cross-Back Training Tank",
                "The most awesome phone of 2013!",
            299.00,
            2,
            "Active Wear - Women"
        );

        Product actualProduct = given().
                queryParam("id", "2").
        when().
                get(endPoint).
                    as(Product.class); // take the response and map the response into this class

        assertThat(actualProduct, samePropertyValuesAs(expectedProduct));
    }
    // Try creating a new product and get the full lifecycle
}
