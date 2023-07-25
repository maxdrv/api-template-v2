package com.home.project.template.metric;

import java.util.List;
import java.util.Map;

public final class Const {

    private Const() {
        throw new RuntimeException("no instance for you");
    }

    public static Map<String, List<String>> WAIT_EVENTS = Map.of(
            "LWLock", List.of("ProcArray", "SubtransBuffer", "SubtransSLRU")
    );

}
