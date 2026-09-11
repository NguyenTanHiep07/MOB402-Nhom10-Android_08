package com.mob10.deliveryserver.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Manages MongoDB indexes manually since auto-index-creation is disabled.
 * Only creates indexes on the root document collections (users, rejection_reasons).
 * Does NOT create indexes on embedded User fields inside delivery_requests to avoid
 * the unique-index-on-null collision when deliveryPerson is null.
 */
@Component
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndexes() {
        // Drop all existing indexes (except _id_) on all relevant collections first.
        for (String col : new String[]{"users", "delivery_requests", "status_histories",
                "order_rejections", "ratings", "driver_statistics", "rejection_reasons",
                "account_challenges", "password_recovery_limits"}) {
            try {
                mongoTemplate.indexOps(col).dropAllIndexes();
            } catch (Exception ignored) {}
        }

        // users collection: unique username and phoneNumber
        mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("username", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("phoneNumber", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("role", Sort.Direction.ASC));

        // delivery_requests: status, createdAt, client, deliveryPerson, compound
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("status", Sort.Direction.ASC));
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("client", Sort.Direction.ASC));
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("deliveryPerson", Sort.Direction.ASC));
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("status", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC));

        // status_histories
        mongoTemplate.indexOps("status_histories")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC));
        mongoTemplate.indexOps("status_histories")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC).on("timestamp", Sort.Direction.ASC));

        // order_rejections
        mongoTemplate.indexOps("order_rejections")
                .ensureIndex(new Index().on("driverId", Sort.Direction.ASC));
        mongoTemplate.indexOps("order_rejections")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC));
        mongoTemplate.indexOps("order_rejections")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC).on("driverId", Sort.Direction.ASC));
        mongoTemplate.indexOps("order_rejections")
                .ensureIndex(new Index().on("driverId", Sort.Direction.ASC).on("penaltyApplied", Sort.Direction.ASC).on("rejectedAt", Sort.Direction.DESC));

        // ratings
        mongoTemplate.indexOps("ratings")
                .ensureIndex(new Index().on("driverId", Sort.Direction.ASC));
        mongoTemplate.indexOps("ratings")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC));

        // driver_statistics
        mongoTemplate.indexOps("driver_statistics")
                .ensureIndex(new Index().on("reliabilityScore", Sort.Direction.ASC));

        // rejection_reasons
        mongoTemplate.indexOps("rejection_reasons")
                .ensureIndex(new Index().on("active", Sort.Direction.ASC));

        // account_challenges
        mongoTemplate.indexOps("account_challenges")
                .ensureIndex(new Index().on("userId", Sort.Direction.ASC));
        mongoTemplate.indexOps("account_challenges")
                .ensureIndex(new Index().on("token", Sort.Direction.ASC));
        mongoTemplate.indexOps("account_challenges")
                .ensureIndex(new Index().on("expiresAt", Sort.Direction.ASC));

        // password_recovery_limits
        mongoTemplate.indexOps("password_recovery_limits")
                .ensureIndex(new Index().on("identifier", Sort.Direction.ASC));
    }
}
