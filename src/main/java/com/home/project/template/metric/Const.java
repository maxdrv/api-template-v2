package com.home.project.template.metric;

import java.util.List;
import java.util.Map;

public final class Const {

    public static final String MASTER = "master";
    public static final String REPLICA_SYNC = "replica_sync";
    public static final String REPLICA_ASYNC = "replica_async";

    public static final Map<String, List<String>> WAIT_EVENTS = Map.of(
            "LWLock", List.of("ProcArray", "SubtransBuffer", "SubtransSLRU")
    );

    public static final Map<String, List<String>> SLRU = Map.of(
            "Subtrans", List.of("blks_hit", "blks_read", "blks_written"),
            "Xact", List.of("blks_hit", "blks_read", "blks_written")
    );

    private Const() {
        throw new RuntimeException("no instance for you");
    }


}
