package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.dto.RatingDtos.CreateRatingRequest;
import com.mob10.deliveryserver.dto.RatingDtos.DriverRatingSummary;
import com.mob10.deliveryserver.dto.RatingDtos.RatingResponse;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import com.mob10.deliveryserver.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingService service;

    public RatingController(RatingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateRatingRequest request) {
        return service.create(user, request);
    }

    @GetMapping
    public RatingResponse getByOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long deliveryRequestId) {
        return service.getByOrder(user, deliveryRequestId);
    }

    @GetMapping("/drivers/{driverId}/summary")
    public DriverRatingSummary getDriverSummary(@PathVariable Long driverId) {
        return service.getDriverSummary(driverId);
    }
}
