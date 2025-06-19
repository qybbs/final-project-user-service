package com.dimaspramantya.user_service.domain.dto.request

import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank

data class ReqValidateOtpDto(
    @field:NotBlank("ID User tidak boleh kosong")
    val userId: Int,
    @field:NotBlank("Kode OTP tidak boleh kosong")
    @field:Digits(integer = 6, fraction = 0, message = "Kode OTP harus berupa angka 6 digit")
    val otpCode: String
)
