package com.mob10.deliveryserver.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mob10.deliveryserver.dto.LocationDtos.AddressSuggestionResponse;
import com.mob10.deliveryserver.dto.LocationDtos.CoordinateInput;
import com.mob10.deliveryserver.exception.ApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Component
public class OpenStreetMapClient {
    private static final String VIETNAM_BBOX = "102.14441,8.179066,109.464638,23.393395";

    private final RestClient photonClient;
    private final RestClient osrmClient;

    public OpenStreetMapClient(
            @Qualifier("photonRestClient") RestClient photonClient,
            @Qualifier("osrmRestClient") RestClient osrmClient) {
        this.photonClient = photonClient;
        this.osrmClient = osrmClient;
    }

    public List<AddressSuggestionResponse> search(String query, int limit) {
        try {
            PhotonResponse response = photonClient.get()
                    .uri(uri -> uri.path("/api/")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .queryParam("bbox", VIETNAM_BBOX)
                            .build())
                    .retrieve()
                    .body(PhotonResponse.class);
            if (response == null || response.features() == null) return List.of();
            return response.features().stream()
                    .map(this::toSuggestion)
                    .filter(Objects::nonNull)
                    .filter(item -> item.country() == null || item.country().isBlank()
                            || item.country().toLowerCase(Locale.ROOT).contains("việt nam")
                            || item.country().toLowerCase(Locale.ROOT).contains("vietnam"))
                    .distinct()
                    .limit(limit)
                    .toList();
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "LOCATION_PROVIDER_TIMEOUT",
                    "Dịch vụ tìm địa chỉ phản hồi quá chậm. Vui lòng thử lại");
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "LOCATION_PROVIDER_UNAVAILABLE",
                    "Dịch vụ tìm địa chỉ đang tạm thời không khả dụng");
        }
    }

    public RouteMetrics route(CoordinateInput pickup, CoordinateInput delivery) {
        String coordinates = pickup.longitude().toPlainString() + "," + pickup.latitude().toPlainString()
                + ";" + delivery.longitude().toPlainString() + "," + delivery.latitude().toPlainString();
        try {
            OsrmResponse response = osrmClient.get()
                    .uri(uri -> uri.path("/route/v1/driving/")
                            .path(coordinates)
                            .queryParam("overview", "false")
                            .queryParam("steps", "false")
                            .queryParam("alternatives", "false")
                            .build())
                    .retrieve()
                    .body(OsrmResponse.class);
            if (response == null || !"Ok".equals(response.code()) || response.routes() == null
                    || response.routes().isEmpty()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTE_NOT_FOUND",
                        "Không tìm thấy tuyến đường phù hợp giữa hai địa chỉ");
            }
            OsrmRoute route = response.routes().getFirst();
            if (route.distance() == null || route.duration() == null || route.distance() <= 0) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTE_NOT_FOUND",
                        "Không tìm thấy tuyến đường phù hợp giữa hai địa chỉ");
            }
            BigDecimal distanceKm = BigDecimal.valueOf(route.distance())
                    .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            int durationMinutes = Math.max(1, (int) Math.ceil(route.duration() / 60.0));
            return new RouteMetrics(distanceKm, durationMinutes);
        } catch (ApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "ROUTE_PROVIDER_TIMEOUT",
                    "Dịch vụ tính tuyến đường phản hồi quá chậm. Vui lòng thử lại");
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ROUTE_PROVIDER_UNAVAILABLE",
                    "Dịch vụ tính tuyến đường đang tạm thời không khả dụng");
        }
    }

    private AddressSuggestionResponse toSuggestion(PhotonFeature feature) {
        if (feature == null || feature.geometry() == null || feature.geometry().coordinates() == null
                || feature.geometry().coordinates().size() < 2 || feature.properties() == null) return null;
        PhotonProperties properties = feature.properties();
        Double longitude = feature.geometry().coordinates().get(0);
        Double latitude = feature.geometry().coordinates().get(1);
        if (longitude == null || latitude == null) return null;

        String streetAddress = joinNonBlank(" ", properties.housenumber(), properties.street());
        String primary = firstNonBlank(streetAddress, properties.name(), properties.street(), properties.locality(), properties.city());
        String ward = firstNonBlank(properties.locality(), properties.district());
        String district = firstNonBlank(properties.district(), properties.county());
        String province = firstNonBlank(properties.state(), properties.city());
        String secondary = joinDistinct(", ", ward, district, properties.city(), properties.state(), properties.postcode());
        String formatted = joinDistinct(", ", primary, secondary, properties.country());
        String placeId = joinNonBlank(":", properties.osmType(), properties.osmId() == null ? null : properties.osmId().toString());

        if (formatted.isBlank()) return null;
        return new AddressSuggestionResponse(
                placeId.isBlank() ? formatted : placeId,
                formatted,
                primary,
                secondary,
                ward,
                district,
                province,
                properties.country(),
                BigDecimal.valueOf(latitude).setScale(7, RoundingMode.HALF_UP),
                BigDecimal.valueOf(longitude).setScale(7, RoundingMode.HALF_UP));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return "";
    }

    private String joinNonBlank(String delimiter, String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) if (value != null && !value.isBlank()) parts.add(value.trim());
        return String.join(delimiter, parts);
    }

    private String joinDistinct(String delimiter, String... values) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) normalized.add(value.trim());
        }
        return String.join(delimiter, normalized);
    }

    public record RouteMetrics(BigDecimal distanceKm, int estimatedDurationMinutes) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PhotonResponse(List<PhotonFeature> features) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PhotonFeature(PhotonGeometry geometry, PhotonProperties properties) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PhotonGeometry(List<Double> coordinates) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PhotonProperties(
            String name,
            String street,
            String housenumber,
            String locality,
            String district,
            String city,
            String county,
            String state,
            String postcode,
            String country,
            @com.fasterxml.jackson.annotation.JsonProperty("osm_type") String osmType,
            @com.fasterxml.jackson.annotation.JsonProperty("osm_id") Long osmId) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OsrmResponse(String code, List<OsrmRoute> routes) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OsrmRoute(Double distance, Double duration) {}
}
