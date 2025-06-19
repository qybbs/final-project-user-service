package com.dimaspramantya.user_service.domain.dto.response

data class ResGetOtpDto(
    val userId: Int,
    val otpCode: String
)
