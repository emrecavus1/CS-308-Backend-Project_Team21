package com.cs308.backend.mailing;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class AbstractEmailContext {

    private String from;
    private String to;
    private String subject;
    private String email;
    private String templateLocation;
    private Map<String, Object> context;

    // Constructor that initializes an empty context
    public AbstractEmailContext() {
        this.context = new HashMap<>();
    }

    // A put() method to add items into the context map
    public void put(String key, Object value) {
        this.context.put(key, value);
    }

    // Option B: Declare an abstract parameterized init() method
    public abstract <T> void init(T context);
}
