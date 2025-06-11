package com.dimaspramantya.user_service.repository

import com.dimaspramantya.user_service.domain.entity.MasterUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MasterUserRepository: JpaRepository<MasterUserEntity, Int> {
    @Query("""
        SELECT U FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
    """, nativeQuery = false)
    fun getAllActiveUser(): List<MasterUserEntity>
}