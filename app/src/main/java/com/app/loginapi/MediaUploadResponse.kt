package com.app.loginapi

data class MediaUploadResponse(
    val base_url: String,
    val data: List<Data>,
    val message: String,
    val status: String
)

data class Data(
    val mediaBaseUrl: String,
    val mediaName: String,
    val medialThumUrl: String,
    val videoThumbImgName: String,
    val videoThumbImgUrl: String
)