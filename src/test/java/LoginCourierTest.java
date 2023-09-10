import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import io.qameta.allure.junit4.DisplayName; // импорт DisplayName
import io.qameta.allure.Description; // импорт Description
import io.qameta.allure.Step; // импорт Step

//тестируем варианты авторизации курьера
public class LoginCourierTest {

    Courier newCourier = new Courier("dodo_4", "1234", "dodoFirstName"); //данные основного курьера
    Credentials credentials = new Credentials(newCourier.getLogin(), newCourier.getPassword()); //логин и пароль основного курьера

public void createCourier(Courier courier) { //создание курьера с целью позже под ним логиниться
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        response.then()
                .statusCode(201);

    }



    @Before
    public void setUp() {

        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }



    @Test
    @DisplayName("Авторизация курьера, успешная")
    public void loginCourierOk() {
        createCourier(newCourier);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(credentials)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        response.then()
                .statusCode(200);
        response
                .then().assertThat().body("id",notNullValue());


    }

    @Test
    @DisplayName("Авторизация курьера без логина, неуспешная")
    public void loginCourierLoginMissedFail() {
        Credentials credentialsLoginMissed = new Credentials("", newCourier.getPassword());
        createCourier(newCourier);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(credentialsLoginMissed)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        response.then()
                .statusCode(400);
        response.then()
                .assertThat().body("message", equalTo("Недостаточно данных для входа"));

    }

    @Test
    @DisplayName("Авторизация курьера без пароля, неуспешная")
    public void loginCourierPasswordMissedFail() {
        Credentials credentialsPasswordMissed = new Credentials(newCourier.getLogin(), "");
        createCourier(newCourier);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(credentialsPasswordMissed)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        response.then()
                .statusCode(400);
        response.then()
                .assertThat().body("message", equalTo("Недостаточно данных для входа"));

    }

    @Test
    @DisplayName("Авторизация курьера с неверным логином и существующим паролем, неуспешная")
    public void loginCourierWrongLoginFail() {
        Credentials wrongLoginCredentials = new Credentials("toto", "1234" );
        createCourier(newCourier);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(wrongLoginCredentials)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        response.then()
                .statusCode(404);
        response.then()
                .assertThat().body("message", equalTo("Учетная запись не найдена"));

    }

    @Test
    @DisplayName("Авторизация курьера с существующим логином и неверным паролем, неуспешная")
    public void loginCourierWrongPasswordFail() {
        Credentials wrongPasswordCredentials = new Credentials("dodo_4", "1534" );
        createCourier(newCourier);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(wrongPasswordCredentials)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        response.then()
                .statusCode(404);
        response.then()
                .assertThat().body("message", equalTo("Учетная запись не найдена"));

    }

   public int loginCourier(Courier courier){ //авторизация курьера с целью получения id

        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(credentials)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        response.then()
                .statusCode(200);
        int courierId = response
                .then().extract().body().path("id");
        return courierId;
    }

    @After
    public void cleanUp() { //удаление курьера
        int id = loginCourier(newCourier);
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .delete(ApiEndpoint.DELETE_COURIER + id);
        response.then().statusCode(200);
    }

}
