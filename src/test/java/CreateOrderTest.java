import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.MatcherAssert;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import io.qameta.allure.junit4.DisplayName; // импорт DisplayName
import io.qameta.allure.Description; // импорт Description
import io.qameta.allure.Step; // импорт Step

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String metroStation;
    private final String phone;
    private final int rentTime;
    private final String deliveryDate;
    private final String comment;
    private final List<String> color;
    private final int statusCode;
    private final boolean isTrack;

    public CreateOrderTest(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, List<String> color, int statusCode, boolean isTrack) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
        this.statusCode = statusCode;
        this.isTrack = isTrack;
    }

    @Parameterized.Parameters
    public static Object[][] OrderTestParam() {
        return new Object[][]{
                {"Вася", "Верещагин", "Филатовский проезд 48-50", "Комсомольская", "89993453434", 3, "2023-10-10", "комментарий комментарий", List.of("BLACK"), 201, true},
                {"Катя", "Верещагина", "Филатовский проезд 48-50", "Киевская", "89996644444", 5, "2023-09-20", "комментарий комментарий", List.of("GREY"), 201, true},
                {"Вера", "Иванова", "Улица 9-го января, 18, 45 ", "Киевская", "89993443434", 10, "2023-09-20", "комментарий комментарий", List.of(""), 201, true},
                {"", "Верещагин", "Филатовский проезд 48-50", "Комсомольская", "89993453434", 3, "", "комментарий комментарий", List.of("BLACK"), 500, false},
                {"Вера", "Иванова", "", "Киевская", "", 10, "2023-09-20", "", List.of(""), 201, true},
                {"Вера", "Иванова", "", "", "", 10, "2023-09-20", "", List.of("BLACK", "GREY"), 201, true},
                {"", "", "", "", "", 10, "2023-09-20", "", List.of("BLACK", "GREY"), 201, true},
                {"Вера", "Иванова", "", "", "", 1, "2023-09-20", "", List.of("BLACK", "GREEN"), 201, true},
                {"Вера", "Иванова", "", "", "", 10, "2023-09-20", "", List.of("BLACK", "VIOLET", "GREY"), 201, true},
                {"Вера", "Иванова", "", "", "", 10, "10-09-2023", "", List.of(""), 201, true},
                {"Вера", "Иванова", "", "", "", 10, "10.09.2023", "", List.of(""), 201, true},
                {"Вера", "Иванова", "", "", "", 10, "10-09-23", "", List.of(""), 201, true},
                {"Вера", "Иванова", "", "", "", 10, "23-09-10", "", List.of(""), 500, false},
         };
    }
     @Before
    public void setUp() {

        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Параметризованный тест создания заказа")
    public void createOrderParamTest(){
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate,comment, color);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(order)
                        .when()
                        .post(ApiEndpoint.CREATE_ORDER);
        response.then().statusCode(statusCode);

        String testStr = response.then().extract().body().asString();
        boolean actualTrack = testStr.contains("track");
        MatcherAssert.assertThat(actualTrack, is(isTrack));

    }
}
