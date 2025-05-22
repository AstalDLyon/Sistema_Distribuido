package com.av2.sistemadistribuidos.validators;

import java.util.regex.Pattern;

import com.av2.sistemadistribuidos.exceptions.ValidationException;

public class DNSRecordValidator {
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]([a-zA-Z0-9\\-.]{0,61}[a-zA-Z0-9])?$"
    );

    public void validateHostname(String hostname) {
        if (isHostnameInvalid(hostname)) {
            throw new ValidationException("Invalid hostname format: " + hostname);
        }
    }

    public void validateIP(String ip) {
        if (isIPInvalid(ip)) {
            throw new ValidationException("Invalid IP format: " + ip);
        }
    }

    private boolean isHostnameInvalid(String hostname) {
        return hostname == null ||
                hostname.trim().isEmpty() ||
                !HOSTNAME_PATTERN.matcher(hostname).matches() ||
                hostname.length() > 253;
    }

    private boolean isIPInvalid(String ip) {
        return ip == null || !IP_PATTERN.matcher(ip).matches();
    }
}