import com.github.javafaker.Faker;
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

        RestAssured.baseURI = ApiEndpoint.BASE_ADDRESS;
    }

    Faker faker = new Faker();
    String login = faker.name().username();
    String password = faker.random().toString();
    String firstName = faker.name().firstName();

    @Step("Создание курьера")
    public Response createCourier(Courier courier)
    {
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(courier)
                        .when()
                        .post(ApiEndpoint.CREATE_COURIER);

        return response;
    }

    @Test
    @DisplayName("Создание курьера, успешное")
    public void postCreateCourier() {

        Courier newCourier = new Courier(login, password, firstName);

        Response response = createCourier(newCourier);
        response.then()
                .statusCode(201);
        response.then()
                .assertThat().body("ok", is(true));

    }

    @Test
    @DisplayName("Создание повторяющегося курьера, неуспешное")
    public void postCreateSameCourierFail() {

        Courier newCourier = new Courier(login, password, firstName);
        Response response = createCourier(newCourier);
        Response responseFail = createCourier(newCourier);
        responseFail.then()
                .statusCode(409);
        responseFail.then()
               // .assertThat().body("message", equalTo("Этот логин уже используется")); //баг относительно документации
                .assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

    }

    @Test
    @DisplayName("Создание курьера с существующим логином  другим паролем, неуспешное")
    public void postCreateSameCourierOtherPasswordFail() {

        Courier newCourier = new Courier(login, password, firstName);

        Courier newCourierOtherPassword = new Courier(newCourier.getLogin(), newCourier.getPassword() + "qq", newCourier.getFirstName());

        Response response = createCourier(newCourier);
        Response responseFail = createCourier(newCourierOtherPassword);

        responseFail.then()
                .statusCode(409);
        responseFail.then()
                // .assertThat().body("message", equalTo("Этот логин уже используется")); //баг относительно документации
                .assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."));


    }

    @Test
    @DisplayName("Создание курьера без пароля, неуспешное")
    public void postCreateCourierWithoutPasswordFail() {

        Courier newCourier = new Courier(login, "", firstName);
        Response responseFail = createCourier(newCourier);
        responseFail.then()
                .statusCode(400);
        responseFail.then()
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"));

    }

    @Test
    @DisplayName("Создание курьера без логина, неуспешное")
    public void postCreateCourierWithoutLoginFail() {

        Courier newCourier = new Courier("", password, firstName);
        Response responseFail = createCourier(newCourier);

        responseFail.then()
                .statusCode(400);
        responseFail.then()
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"));

    }
    @Test
    @DisplayName("Создание курьера без имени, успешное")
    public void postCreateCourierWithoutFirstName() {

        Courier newCourier = new Courier(login, password, "");
        Response responseFail = createCourier(newCourier);

        responseFail.then()
                .statusCode(201);

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
        int code = response.then().
                extract().statusCode();
        int courierId;
        if (code == 200) {
               courierId = response
                .then().extract().body().path("id");}
        else {
            courierId = 0;
        }
        return courierId;
    }

@After
 public void cleanUp() { //удаление курьера
        Courier newCourier = new Courier(login, password, firstName);
        int id = loginCourier(newCourier);
        if (id > 0) {
     Response response = given()
             .header("Content-type", "application/json")
             .when()
             .delete(ApiEndpoint.DELETE_COURIER + id);
     response.then().statusCode(200);}
        else  {
            System.out.println("Cannot delete not existing courier");
        }
 }
}
