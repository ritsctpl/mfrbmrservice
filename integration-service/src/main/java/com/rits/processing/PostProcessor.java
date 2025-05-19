package com.rits.processing;

import java.util.Map;

public interface PostProcessor {
    Map<String, Object> process(Map<String, Object> response);
}
