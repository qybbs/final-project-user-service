package com.dimaspramantya.user_service.repository

import com.dimaspramantya.user_service.domain.entity.MasterUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface MasterUserRepository: JpaRepository<MasterUserEntity, Int> {
    @Query("""
        SELECT U FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
    """, nativeQuery = false)
    fun getAllActiveUser(): List<MasterUserEntity>

    fun findFirstByEmail(email: String): MasterUserEntity?

    @Query("""
        SELECT U FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
        AND U.username = :username
    """, nativeQuery = false)
    fun findFirstByUsername(username: String): Optional<MasterUserEntity>

    @Query("""
        SELECT u FROM MasterUserEntity u
        WHERE u.id IN (:ids)
    """, nativeQuery = false)
    fun findAllByIds(ids: List<Int>): List<MasterUserEntity>
}