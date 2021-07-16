import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class ReqResTests3 {

    @Test
    public void getSingleUserTest(){
        RestAssured
                .given()
                .log().all()
                .contentType(ContentType.JSON)
                .get("https://reqres.in/api/users/2")
                .then()
                .log().all()
                .statusCode(200)
                .body("data.id", equalTo(2));
    }
}
