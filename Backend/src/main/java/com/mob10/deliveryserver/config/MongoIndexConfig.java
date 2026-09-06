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
        // users collection: unique username and phoneNumber
        mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("username", Sort.Direction.ASC).unique().named("idx_users_username"));
        mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("phoneNumber", Sort.Direction.ASC).unique().named("idx_users_phone"));
        mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("role", Sort.Direction.ASC).named("idx_users_role"));

        // delivery_requests: only non-unique indexes — drop any stale embedded ones first
        dropEmbeddedUserIndexes();
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("status", Sort.Direction.ASC).named("idx_orders_status"));
        mongoTemplate.indexOps("delivery_requests")
                .ensureIndex(new Index().on("createdAt", Sort.Direction.DESC).named("idx_orders_created"));

        // status_histories
        mongoTemplate.indexOps("status_histories")
                .ensureIndex(new Index().on("deliveryRequestId", Sort.Direction.ASC).named("idx_history_order"));

        // ratings
        mongoTemplate.indexOps("ratings")
                .ensureIndex(new Index().on("driverId", Sort.Direction.ASC).named("idx_ratings_driver"));
    }

    private void dropEmbeddedUserIndexes() {
        // Drop any leftover embedded-user indexes that were created before @DocumentReference
        for (String col : new String[]{"delivery_requests", "status_histories", "order_rejections", "ratings", "driver_statistics"}) {
            try {
                var indexOps = mongoTemplate.indexOps(col);
                indexOps.getIndexInfo().forEach(info -> {
                    String name = info.getName();
                    // Drop any index whose name contains an embedded field path (has a dot in the field name)
                    if (!name.equals("_id_") && (name.contains(".username") || name.contains(".phoneNumber")
                            || name.contains(".role") || name.contains("deliveryPerson") || name.contains("client."))) {
                        try { indexOps.dropIndex(name); } catch (Exception ignored) {}
                    }
                });
            } catch (Exception ignored) {}
        }
    }
}
