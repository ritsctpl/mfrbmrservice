package com.rits.processing;

import java.util.Map;

public interface PreProcessor {
    Map<String, Object> process(Map<String, Object> message);
}
