package com.dimaspramantya.user_service.domain.dto.request

data class ReqTransferDto(
    val from: String,
    val to: String,
    val score: Int
)
