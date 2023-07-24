package com.home.project.template.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public final class UrlUtil {

    private static final String PG_PROTOCOL = "jdbc:postgresql://";

    private UrlUtil() {
        // util class
    }

    public static List<String> splitPgUrlByHosts(String pgUrl) {
        String hostsAndDbParameters = validateAndTrimProtocol(pgUrl);

        String hostAndPortOnly = StringUtils.substringBefore(hostsAndDbParameters, "/");
        String dbParameters = StringUtils.substringAfter(hostsAndDbParameters, "/");

        return Arrays.stream(hostAndPortOnly.split(","))
                .map(host -> PG_PROTOCOL + host + "/" + dbParameters)
                .toList();
    }

    public static String getHostFromPgUrl(String pgUrl) {
        String hostsAndDbParameters = validateAndTrimProtocol(pgUrl);

        if (hostsAndDbParameters.contains(":")) {
            // explicit port
            return StringUtils.substringBefore(hostsAndDbParameters, ":");
        }
        return StringUtils.substringBefore(hostsAndDbParameters, "/");
    }

    private static String validateAndTrimProtocol(String pgUrl) {
        pgUrl = pgUrl.trim();
        if (!pgUrl.startsWith(PG_PROTOCOL)) {
            throw new IllegalArgumentException(
                    "Only postgres url is allowed, unknown protocol in: " + pgUrl
            );
        }

        String hostsAndDbParameters = StringUtils.substringAfter(pgUrl, PG_PROTOCOL);
        if (!hostsAndDbParameters.contains("/")) {
            throw new IllegalArgumentException(
                    "Only postgres url is allowed, nothing after host: " + pgUrl
            );
        }

        return hostsAndDbParameters;
    }


}
