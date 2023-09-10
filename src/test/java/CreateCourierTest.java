import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import io.qameta.allure.junit4.DisplayName; // импорт DisplayName
import io.qameta.allure.Description; // импорт Description
import io.qameta.allure.Step; // импорт Step

import java.io.File;

public class CreateCourierTest {

    @Before
    public void setUp() {

        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Создание курьера, успешное")
    public void postCreateCourier() {

        Courier newCourier = new Courier("dodo_4", "1234", "dodoFirstName");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        response.then()
                .statusCode(201);
        response.then()
                .assertThat().body("ok", is(true));
        cleanUp(newCourier);
    }

    @Test
    @DisplayName("Создание повторяющегося курьера, неуспешное")
    public void postCreateSameCourierFail() {

        Courier newCourier = new Courier("dodo_4", "1234", "dodoFirstName");
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);

        Response responseFail =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        responseFail.then()
                .statusCode(409);
        responseFail.then()
               // .assertThat().body("message", equalTo("Этот логин уже используется")); //баг относительно документации
                .assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
        cleanUp(newCourier);
    }

    @Test
    @DisplayName("Создание курьера с существующим логином  другим паролем, неуспешное")
    public void postCreateSameCourierOtherPasswordFail() {

        Courier newCourier = new Courier("dodo_4", "1234", "dodoFirstName");

        Courier newCourierOtherPassword = new Courier("dodo_4", "1235", "dodoFirstName");
            Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);

        Response responseFail =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourierOtherPassword)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        responseFail.then()
                .statusCode(409);
        responseFail.then()
                // .assertThat().body("message", equalTo("Этот логин уже используется")); //баг относительно документации
                .assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

        cleanUp(newCourier);

    }

    @Test
    @DisplayName("Создание курьера без пароля, неуспешное")
    public void postCreateCourierWithoutPasswordFail() {

        Courier newCourier = new Courier("dodo_4", "", "dodoFirstName");

        Response responseFail =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        responseFail.then()
                .statusCode(400);
        responseFail.then()
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"));

    }

    @Test
    @DisplayName("Создание курьера без логина, неуспешное")
    public void postCreateCourierWithoutLoginFail() {

        Courier newCourier = new Courier("", "1234", "dodoFirstName");

        Response responseFail =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        responseFail.then()
                .statusCode(400);
        responseFail.then()
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"));

    }
    @Test
    @DisplayName("Создание курьера без имени, успешное")
    public void postCreateCourierWithoutFirstName() {

        Courier newCourier = new Courier("dodo_4", "1234", "");

        Response responseFail =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(newCourier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);
        responseFail.then()
                .statusCode(201);
        cleanUp(newCourier);
    }

    public int loginCourier(Courier courier){ //авторизация курьера, с целью получения его id

        Credentials credentials = new Credentials(courier.getLogin(), courier.getPassword());
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


 public void cleanUp(Courier courier) { //удаление курьера
        int id = loginCourier(courier);
     Response response = given()
             .header("Content-type", "application/json")
             .when()
             .delete(ApiEndpoint.DELETE_COURIER + id);
     response.then().statusCode(200);
 }
}
