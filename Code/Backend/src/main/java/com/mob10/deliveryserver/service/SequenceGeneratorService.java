package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.DatabaseSequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {
    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public long generateSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                DatabaseSequence.class);
        return counter != null ? counter.getSeq() : 1L;
    }

    public void setSequenceIfGreater(String seqName, long value) {
        DatabaseSequence counter = mongoOperations.findById(seqName, DatabaseSequence.class);
        if (counter == null || counter.getSeq() < value) {
            mongoOperations.save(new DatabaseSequence(seqName, value));
        }
    }
}
