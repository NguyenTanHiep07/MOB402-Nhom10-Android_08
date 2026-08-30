package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.dto.LocationDtos.CoordinateInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenStreetMapClientTest {
    private MockRestServiceServer photonServer;
    private MockRestServiceServer osrmServer;
    private OpenStreetMapClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder photonBuilder = RestClient.builder().baseUrl("https://photon.test");
        RestClient.Builder osrmBuilder = RestClient.builder().baseUrl("https://osrm.test");
        photonServer = MockRestServiceServer.bindTo(photonBuilder).build();
        osrmServer = MockRestServiceServer.bindTo(osrmBuilder).build();
        client = new OpenStreetMapClient(photonBuilder.build(), osrmBuilder.build());
    }

    @Test
    void mapsPhotonGeoJsonToStableApiContract() {
        photonServer.expect(requestTo(containsString("/api/")))
                .andRespond(withSuccess("""
                        {
                          "features": [{
                            "properties": {
                              "osm_type": "W",
                              "osm_id": 189067626,
                              "name": "Bệnh viện Nguyễn Trãi",
                              "street": "Đường Nguyễn Trãi",
                              "housenumber": "314",
                              "locality": "Khu phố 18",
                              "district": "An Đông",
                              "city": "Thành phố Hồ Chí Minh",
                              "country": "Việt Nam",
                              "postcode": "72760"
                            },
                            "geometry": {"coordinates": [106.6750459, 10.7568827]}
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        var results = client.search("Nguyễn Trãi", 6);

        assertEquals(1, results.size());
        assertEquals("W:189067626", results.getFirst().placeId());
        assertEquals("314 Đường Nguyễn Trãi", results.getFirst().primaryText());
        assertEquals("Khu phố 18", results.getFirst().ward());
        assertEquals(new BigDecimal("10.7568827"), results.getFirst().latitude());
        photonServer.verify();
    }

    @Test
    void convertsOsrmMetersAndSecondsToKilometersAndMinutes() {
        osrmServer.expect(requestTo(containsString("/route/v1/driving/106.6800,10.7700;106.7100,10.8000")))
                .andRespond(withSuccess("""
                        {
                          "code": "Ok",
                          "routes": [{"distance": 5408.5, "duration": 422.8}]
                        }
                        """, MediaType.APPLICATION_JSON));

        var result = client.route(
                new CoordinateInput(new BigDecimal("10.7700"), new BigDecimal("106.6800")),
                new CoordinateInput(new BigDecimal("10.8000"), new BigDecimal("106.7100")));

        assertEquals(new BigDecimal("5.41"), result.distanceKm());
        assertEquals(8, result.estimatedDurationMinutes());
        osrmServer.verify();
    }
}
