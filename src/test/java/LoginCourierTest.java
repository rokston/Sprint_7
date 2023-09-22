import com.github.javafaker.Faker;
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
    Faker faker = new Faker();
    String login = faker.name().username();
    String password = faker.random().toString();
    String firstName = faker.name().firstName();

  //данные основного курьера
    Courier newCourier = new Courier(login, password, firstName);
    Credentials credentials = new Credentials(newCourier.getLogin(), newCourier.getPassword()); //логин и пароль основного курьера

    @Step("Создание курьера")
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

    @Step("логин курьера")
    public Response loginCourier(Credentials credentials){
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(credentials)
                        .when()
                        .post(ApiEndpoint.LOGIN_COURIER);
        return response;
    }

    @Step("удаление курьера")
    public Response deleteCourier(int id){
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .delete(ApiEndpoint.DELETE_COURIER + id);
        return response;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = ApiEndpoint.BASE_ADDRESS;
    }



    @Test
    @DisplayName("Авторизация курьера, успешная")
    public void loginCourierOk() {
        createCourier(newCourier);
        Response response = loginCourier(credentials);
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
        Response response = loginCourier(credentialsLoginMissed);

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
        Response response = loginCourier(credentialsPasswordMissed);

        response.then()
                .statusCode(400);
        response.then()
                .assertThat().body("message", equalTo("Недостаточно данных для входа"));

    }

    @Test
    @DisplayName("Авторизация курьера с неверным логином и существующим паролем, неуспешная")
    public void loginCourierWrongLoginFail() {

        createCourier(newCourier);
        Credentials wrongLoginCredentials = new Credentials(newCourier.getLogin() + "oops", newCourier.getPassword());
        Response response = loginCourier(wrongLoginCredentials);

        response.then()
                .statusCode(404);
        response.then()
                .assertThat().body("message", equalTo("Учетная запись не найдена"));

    }

    @Test
    @DisplayName("Авторизация курьера с существующим логином и неверным паролем, неуспешная")
    public void loginCourierWrongPasswordFail() {
        createCourier(newCourier);
        Credentials wrongPasswordCredentials = new Credentials(newCourier.getLogin(), newCourier.getPassword() + "ff" );
        Response response = loginCourier(wrongPasswordCredentials);

        response.then()
                .statusCode(404);
        response.then()
                .assertThat().body("message", equalTo("Учетная запись не найдена"));

    }

   public int loginCourier(Courier courier){ //авторизация курьера с целью получения id
        Response response = loginCourier(credentials);

        response.then()
                .statusCode(200);
        int courierId = response
                .then().extract().body().path("id");
        return courierId;
    }


    @After
    public void cleanUp() { //удаление курьера
        int id = loginCourier(newCourier);
        Response response = deleteCourier(id);
        response.then().statusCode(200);
    }

}
