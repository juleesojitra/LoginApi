package com.app.loginapi

data class LoginRequest(
    val `data`: Data
) {
    data class Data(
        val langType: String,
        val deviceId: String,
        val deviceType: String,
        val email: String,
        val password: String,
        val timezone: String
    )
}

