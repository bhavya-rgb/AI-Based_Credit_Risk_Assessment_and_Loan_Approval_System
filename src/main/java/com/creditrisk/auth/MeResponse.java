package com.creditrisk.auth;

import java.util.List;

public record MeResponse(Long userId, String email, String fullName, List<String> roles) {}
