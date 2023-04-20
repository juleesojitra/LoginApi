package com.app.loginapi.request

data class SocialLoginRequest(
    val `data`: Data
) {
    data class Data(
        val auth_id: String,
        val auth_provider: String,
        val deviceId: String,
//        val deviceToken: String,
        val deviceType: String,
        val isManualEmail: String,
        val langType: String,
        val firstName: String,
        val email: String,
        val timeZone: String,
        var image: String,
    )
}
