package gmail.salokin1991.tests;

import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static gmail.salokin1991.filters.CustomLogFilter.customLogFilter;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class HWBookStoreTests extends TestBase {

    @Test
    @Tag("API")
    @Disabled
    void authorizeWithSchemeTest() {

        String data = "{" +
                "  \"userName\": \"alex\"," +
                "  \"password\": \"asdsad#frew_DFS2\"" +
                "}";

        given()
                .filter(new AllureRestAssured())
                .contentType("application/json")
                .accept("application/json")
                .body(data.toString())
                .when()
                .log().uri()
                .log().body()
                .post("https://demoqa.com/Account/v1/GenerateToken")
                .then()
                .log().body()
                .body(matchesJsonSchemaInClasspath("sсhemas/GenerateTokenSсheme.json"))
                .body("status", is("Success"))
                .body("result", is("User authorized successfully."));
    }

    @Test
    @Tag("API")
    @Disabled
    void deleteBookWithoutAuthorizationTest() {

        String data = "{\"UserId\": \"1\"}";

        given()
                .filter(customLogFilter().withCustomTemplates())
                .contentType("application/json")
                .accept("application/json")
                .body(data)
                .when()
                .delete("/BookStore/v1/Books")
                .then()
                .log().body()
                .body("code", is("1200"))
                .body("message", is("User not authorized!"));
    }

    @Test
    @Tag("HW_19")
    @Disabled
    void addToNewUserWishlistTest() {
        given()
                .filter(new AllureRestAssured())
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("addtocart_43.EnteredQuantity=1")
                .when()
                .log().all()
                .post("/addproducttocart/details/43/2")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("updatetopwishlistsectionhtml", is("(1)"))
                .body("message", is("The product has been added to your " +
                        "\u003ca href=\"/wishlist\"\u003ewishlist\u003c/a\u003e"));
    }

    @Test
    @Tag("HW_19")
    void OneWishTest() {

        final String LOGIN = "test_demowebshop_91@gmail.ru";
        final String PASSWORD = "test1234";

        step("login and adding item to wishlist by API", () -> {

            Map<String, String> authorizationCookies =
                    given()
                            .filter(customLogFilter().withCustomTemplates())
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .formParam("Email", LOGIN)
                            .formParam("Password", PASSWORD)
                            .when()
                            .log().all()
                            .post("/login")
                            .then()
                            .log().all()
                            .statusCode(302)
                            .extract()
                            .cookies();

            given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .cookies(authorizationCookies)
                    .body("addtocart_43.EnteredQuantity=1")
                    .when()
                    .post("/addproducttocart/details/43/2")
                    .then()
                    .statusCode(200)
                    .body("success", is(true))
                    .body("updatetopwishlistsectionhtml", is(equalTo("(1)")));

            String authorizationCookie = authorizationCookies.get("NOPCOMMERCE.AUTH");

            step("open logo", () ->
                    open("/Themes/DefaultClean/Content/images/logo.png"));

            step("adding cookies", () ->
                    getWebDriver().manage().addCookie(new Cookie("NOPCOMMERCE.AUTH", authorizationCookie)));
        });

        step("Go to wishlist", () ->
                open("/wishlist"));

        step("Setting value of wishes", () ->
                $(".qty-input").setValue("0"));

        step("Update wishlist", () ->
                $(".button-2.update-wishlist-button").click());

        step("And finally", () ->
                $(".wishlist-qty").shouldHave(text("(0)")));
    }
}
