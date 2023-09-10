import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.MatcherAssert;
import org.junit.*;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import io.qameta.allure.junit4.DisplayName; // импорт DisplayName

public class GetOrderListTest { //получение списка заказов, проверка, что в ответе имеется список заказов
    @Before
    public void setUp() {

        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Получение полного списка заказов")
    public void getOrderListFull() {

        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .get(ApiEndpoint.GET_ORDER_LIST);
        response.then().statusCode(200); //правильный статус код ответа
        response.then().assertThat() .body("orders",notNullValue()); // в теле ответа есть orders и потом непусто
        List<Order> orders = response.then().extract().body().jsonPath().getList("orders", Order.class); //десериализация в список заказов
        MatcherAssert.assertThat(orders.isEmpty(),is(false)); //проверяем, что список заказов непуст

    }
    @Test
    @DisplayName("Получение первых 10 заказов из списка")
    public void getOrderList10() {

        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .get(ApiEndpoint.GET_ORDER_LIST + "?limit=10&page=0"); //получаем список
        response.then().statusCode(200); //правильный код ответа
        response.then().assertThat() //проверка, что после orders в ответе непусто
                .body("orders",
                        notNullValue());
        List<Order> orders = response.then().extract().body().jsonPath().getList("orders", Order.class);
        //десериализация ответа в список заказов
        MatcherAssert.assertThat(orders.size(),is(10));//размер списка = 10

    }

    @Test
    @DisplayName("Получение первых 10 заказов из списка с фильтром по станции метро")
    public void getOrderList10_110() {

        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .get(ApiEndpoint.GET_ORDER_LIST + "?limit=10&page=0&nearestStation=[\"110\"]");//получение заказов с фильтром по станции 110
        response.then().statusCode(200); //правильный код ответа
        response.then().assertThat() // после orders в ответе непусто
                .body("orders",
                        notNullValue());
        List<Order> orders = response.then().extract().body().jsonPath().getList("orders", Order.class);
        //десериализовали ответ в список заказов
        MatcherAssert.assertThat(orders.size(),is(10)); //проверили его размер

    }

}
