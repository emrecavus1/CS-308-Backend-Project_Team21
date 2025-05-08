// utils/MongoIdUtils.java
package com.cs308.backend.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.bson.types.ObjectId;

public class MongoIdUtils {
    public static LocalDateTime extractTimestampFromObjectId(String objectId) {
        ObjectId oid = new ObjectId(objectId);
        Instant instant = Instant.ofEpochSecond(oid.getTimestamp());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
