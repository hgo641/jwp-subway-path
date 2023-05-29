package subway.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import subway.business.service.LineService;
import subway.business.service.dto.*;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static subway.fixtures.station.StationFixture.성수역;
import static subway.fixtures.station.StationFixture.잠실역;

@Sql("classpath:station_data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LineControllerIntegratedTest {
    @Autowired
    private LineService lineService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("DELETE FROM line");
    }

    @DisplayName("노선을 생성한다.")
    @Test
    void shouldCreateLineWhenRequest() {
        LineSaveRequest lineSaveRequest = new LineSaveRequest(
                "2호선",
                1L,
                2L,
                5,
                0);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(lineSaveRequest)
                .when().post("/lines")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("노선에 역을 추가한다.")
    @Test
    void shouldAddStationToLineWhenRequest() {
        LineSaveRequest lineSaveRequest = new LineSaveRequest(
                "2호선",
                1L,
                2L,
                5,
                0);
        LineResponse lineResponse = lineService.createLine(lineSaveRequest);

        StationAddToLineRequest stationAddToLineRequest = new StationAddToLineRequest(
                3L,
                2L,
                "상행",
                3
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(stationAddToLineRequest)
                .when().post("/lines/" + lineResponse.getId() + "/station")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("노선에 포함된 역 한 개를 제외한다.")
    @Test
    void shouldRemoveStationFromLineWhenRequest() {
        LineSaveRequest lineSaveRequest = new LineSaveRequest(
                "2호선",
                1L,
                2L,
                5,
                0);
        lineService.createLine(lineSaveRequest);

        StationAddToLineRequest stationAddToLineRequest = new StationAddToLineRequest(
                3L,
                2L,
                "상행",
                3
        );
        lineService.addStationToLine(1L, stationAddToLineRequest);

        StationDeleteRequest stationDeleteRequest = new StationDeleteRequest(3L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(stationDeleteRequest)
                .when().delete("/lines/1/station")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

    }

    @DisplayName("특정 노선의 이름과 포함된 모든 역을 조회한다.")
    @Test
    void shouldReturnLineAndStationsWhenRequest() {
        LineSaveRequest lineSaveRequest = new LineSaveRequest(
                "2호선",
                1L,
                2L,
                5,
                0);
        LineResponse lineResponse = lineService.createLine(lineSaveRequest);

        RestAssured.given().log().all()
                .when().get("/lines/" + lineResponse.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("name", is("2호선"))
                .body("stations.id", hasItems(1, 2))
                .body("stations.name", hasItems("강남역", "역삼역"));
    }

    @DisplayName("모든 노선의 이름과 포함된 모든 역을 조회한다.")
    @Test
    void shouldReturnAllLinesAndStationsWhenRequest() {
        LineSaveRequest lineSaveRequest1 = new LineSaveRequest(
                "2호선",
                1L,
                2L,
                5,
                0);
        lineService.createLine(lineSaveRequest1);

        LineSaveRequest lineSaveRequest2 = new LineSaveRequest(
                "3호선",
                3L,
                4L,
                5,
                0);
        lineService.createLine(lineSaveRequest2);

        RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("[0].name", is("2호선"))
                .body("[0].stations.id", hasItems(1, 2))
                .body("[0].stations.name", hasItems("강남역", "역삼역"))
                .body("[1].name", is("3호선"))
                .body("[1].stations.id", hasItems(3, 4))
                .body("[1].stations.name", hasItems("잠실역", "성수역"));
    }
}
