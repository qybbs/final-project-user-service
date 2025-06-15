package com.dimaspramantya.user_service.repository

import com.dimaspramantya.user_service.domain.entity.ScoreEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ScoreRepository: JpaRepository<ScoreEntity, Long> {
    fun findFirstByName(name: String): Optional<ScoreEntity>
}