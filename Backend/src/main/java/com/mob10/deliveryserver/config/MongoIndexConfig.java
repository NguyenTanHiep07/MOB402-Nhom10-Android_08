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
        // This is needed because Atlas may have stale indexes from previous runs
        // (e.g., embedded-user unique indexes, or differently-named indexes).
        for (String col : new String[]{"users", "delivery_requests", "status_histories",
                "order_rejections", "ratings", "driver_statistics"}) {
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

        // delivery_requests: only non-unique indexes
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("status", Sort.Direction.ASC));
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("createdAt", Sort.Direction.DESC));

        // status_histories
        mongoTemplate.indexOps("status_histories")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC));

        // ratings
        mongoTemplate.indexOps("ratings")
                .ensureIndex(new Index().on("driverId", Sort.Direction.ASC));
    }
}
