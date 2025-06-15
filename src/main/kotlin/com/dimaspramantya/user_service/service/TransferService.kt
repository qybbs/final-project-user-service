package com.dimaspramantya.user_service.service

import com.dimaspramantya.user_service.domain.entity.ScoreEntity

interface TransferService {
    fun processTransfer(from: ScoreEntity, to: ScoreEntity, score: Int): String
}