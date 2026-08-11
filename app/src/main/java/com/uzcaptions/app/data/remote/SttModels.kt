package com.uzcaptions.app.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SttSuccessResponse(val text: String? = null)

@JsonClass(generateAdapter = true)
data class SttErrorResponse(val detail: String? = null)
