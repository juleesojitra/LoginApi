package com.app.loginapi

data class LoginResponse(
    val `data`: Data,
    val message: String,
    val status: String
) {
    data class Data(
        val address: String,
        val allNotification: String,
        val ambulance: String,
        val createdDate: String,
        val email: String,
        val emergencyNumber: String,
        val fillpassword: String,
        val forgotCode: String,
        val gender: String,
        val id: String,
        val image: String,
        val isSocialConnect: String,
        val latitude: String,
        val longitude: String,
        val name: String,
        val password: String,
        val phone: String,
        val police: String,
        val profileStatus: String,
        val profileimage: String,
        val refferalCode: String,
        val role: String,
        val status: String,
        val thumbprofileimage: String,
        val timezone: String,
        val token: String,
        val updatedDate: String,
        val verificationCode: String,
        val zipcode: String
    )

}