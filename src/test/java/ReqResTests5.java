import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReqResTests5 {

    private static final Logger logger = LogManager.getLogger(ReqResTests5.class);

    @BeforeEach
    public void setup(){
        logger.info("Iniciando la configuracion");
        RestAssured.requestSpecification = defaultRequestSpecification();
/*        RestAssured.baseURI = "https://reqres.in";
        RestAssured.basePath = "/api";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();*/
        logger.info("Configuracion exitosa");
    }


    @Test
    public void loginTest(){

                given()
                .body("{\n" +
                        "    \"email\": \"eve.holt@reqres.in\",\n" +
                        "    \"password\": \"cityslicka\"\n" +
                        "}")
                .post("login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("token", notNullValue());
    }

    @Test
    public void getSingleUserTest(){
                given()
                .get("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.id", equalTo(2));
    }

    @Test
    public void deleteUserTest(){
        given()
                .delete("users/2")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void patchUserTest(){
        String nameUpdated = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                .patch("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)

                // se accede a las propiedades del arbol con jsonpath
                .extract().jsonPath().getString("name");
                assertThat(nameUpdated, equalTo("morpheus"));
    }

    @Test
    public void putUserTest(){
        String jobUpdated = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                .put("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)

                // se accede a las propiedades del arbol con jsonpath
                .extract().jsonPath().getString("job");
        assertThat(jobUpdated, equalTo("zion resident"));
    }

    @Test
    public void getAllUsersTest(){
        Response response = given()
                .get("users?page=2");

        Headers headers = response.getHeaders();
        int statusCode = response.getStatusCode();
        String body = response.getBody().asString();
        String contentType = response.getContentType();

        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        System.out.println("body: " + body);
        System.out.println("header: " + headers);
        System.out.println("contentType: " + contentType);
    }

    @Test
    public void getAllUsersTest2(){
        String response = given()
                .get("users?page=2").then().extract().body().asString();
        int page = from(response).get("page");
        int totalPages = from(response).get("total_pages");
        int idFirstUser = from(response).get("data[0].id");

        System.out.println("pagina: " + page);
        System.out.println("total paginas: " + totalPages);
        System.out.println("id primer usuario: " + idFirstUser);

        List<Map> usersWithIdGreaterThan10 = from(response).get("data.findAll { user -> user.id > 10 }");
        String email = usersWithIdGreaterThan10.get(0).get("email").toString();

        List<Map> user = from(response).get("data.findAll { user -> user.id > 10  && user.last_name == 'Howell'}");
        String id = user.get(0).get("id").toString();
    }

    @Test
    public void createUserTest(){
        String response = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"leader\"\n" +
                        "}")
                .post("users")
                .then().extract().body().asString();
        User user = from(response).getObject("", User.class);

        System.out.println(user.getId());
        System.out.println(user.getJob());
    }

    @Test
    public void registerUserTest(){
        CreateUserRequest user = new CreateUserRequest();
        user.setEmail("eve.holt@reqres.in");
        user.setPassword("pistol");

        CreateUserResponse userResponse = given()
                .when()
                .body(user)
                .post("register")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().body().as(CreateUserResponse.class);
        assertThat(userResponse.getId(), equalTo(4));
        assertThat(userResponse.getToken(), equalTo("QpwL5tke4Pnpja7X4"));
    }

    private RequestSpecification defaultRequestSpecification(){
        List<Filter> filters = new ArrayList<>();
        filters.add(new RequestLoggingFilter());
        filters.add(new ResponseLoggingFilter());

        return new RequestSpecBuilder().setBaseUri("https://reqres.in")
                .setBasePath("/api")
                .setContentType(ContentType.JSON).build();
    }
}
