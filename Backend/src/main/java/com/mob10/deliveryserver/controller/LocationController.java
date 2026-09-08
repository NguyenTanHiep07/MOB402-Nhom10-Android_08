package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.dto.LocationDtos.AddressSuggestionResponse;
import com.mob10.deliveryserver.dto.LocationDtos.RouteEstimateRequest;
import com.mob10.deliveryserver.dto.LocationDtos.RouteEstimateResponse;
import com.mob10.deliveryserver.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/locations/autocomplete")
    public List<AddressSuggestionResponse> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "6") int limit) {
        return locationService.autocomplete(query, limit);
    }

    @PostMapping("/routes/estimate")
    public RouteEstimateResponse estimate(@Valid @RequestBody RouteEstimateRequest request) {
        return locationService.estimate(request);
    }
}
