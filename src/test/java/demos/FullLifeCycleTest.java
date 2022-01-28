package demos;

import com.ibm.icu.math.BigDecimal;
import models.Product;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullLifeCycleTest {
    String readOneEndPoint = "http://localhost:8888/api_testing/product/read_one.php";
    String createProductEndPoint = "http://localhost:8888/api_testing/product/create.php";
    String productName = "Football Boots";
    String productDescription = "Adidas predator multi ground boots. Size UK8.5";
    String justRead = "http://localhost:8888/api_testing/product/read.php";
    private int productId;
    private final int categoryId = 99;
    private final double price = 89.99;
    private final String updateProductEndPoint = "http://localhost:8888/api_testing/product/update.php";
    private final String deleteProductEndPoint = "http://localhost:8888/api_testing/product/delete.php";

    // Before creating a new product and posting it to the database, ensure it does not exist
    @Test
    @Order(1)
    public void productDoesNotExist(){
        var ans = given().
                queryParam("name", this.productName).
        when().
                get(this.readOneEndPoint).
        then().assertThat().
                statusCode(200).
                body(blankOrNullString());

        ans.log().body();
    }

    @Test
    @Order(2)
    public void createProduct(){

        // call the Product PUT constructor
        Product newProduct = new Product(
                this.productName,
                this.productDescription,
                this.price,
                this.categoryId
        );
        
        given().body(newProduct).when().post(this.createProductEndPoint).then().
        assertThat().
            statusCode(201).
            body("message", equalTo("Product was created."));
    }

    @Test
    @Order(3)
    public void getCreatedProduct(){
        int lastIdValue = this.getLastIdValueFromDataBase();
        Product expectedProduct = new Product(
                lastIdValue,
                this.productName,
                this.productDescription,
                this.price,
                this.categoryId,
                null
        );
        Product actualProduct = given().queryParam("id", lastIdValue).
                when().get(this.readOneEndPoint).as(Product.class);
        assertThat(actualProduct, samePropertyValuesAs(expectedProduct));
    }

    @Test
    @Order(4)
    public void updateCreatedProduct() {
        this.productId = this.getLastIdValueFromDataBase();
        BigDecimal reducedPrice =  BigDecimal.valueOf(this.price).subtract(BigDecimal.valueOf(50));
        Product expectedUpdatedProduct = new Product(
                this.productId,
                this.productName,
                this.productDescription,
                reducedPrice.doubleValue(),
                this.categoryId,
                null
        );

        String updateTemplate = """
                {
                "id": "%s",
                "name": "%s",
                "description": "%s",
                "price": "%s",
                "category_id": "%s"
                }
                """;
        String updateValues = updateTemplate.formatted(
                productId,
                this.productName,
                this.productDescription,
                reducedPrice.doubleValue(),
                this.categoryId
        );
        given().body(updateValues).when().put(this.updateProductEndPoint).then().
        assertThat().
            statusCode(200).
            body("message", equalTo("Product updated"));


        // also check via the returned object.
        Product actualProduct = given().queryParam("id", productId).when().get(this.readOneEndPoint).as(Product.class);
        assertThat(actualProduct, samePropertyValuesAs(expectedUpdatedProduct));
    }

    public int getLastIdValueFromDataBase(){
        ArrayList<String> value = given().when().get(this.justRead).then().extract().path("records.id");
        ArrayList<Integer> intValue = new ArrayList<>();
        for(String s: value) intValue.add(Integer.valueOf(s));
        return Collections.max(intValue);
    }

    @Test
    @Order(5)
    public void deleteCreatedProduct() {
        String payload = """
                {
                    "id": "%d"
                }
                """;
        String itemToDelete = String.format(payload, this.getLastIdValueFromDataBase());
        given().body(itemToDelete).when().delete(this.deleteProductEndPoint).then().
            assertThat().
                statusCode(200).
                body("message", equalTo("Product was deleted."));
    }
}
