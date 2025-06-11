package com.dimaspramantya.user_service.controller

import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.service.MasterUserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val masterUserService: MasterUserService
) {
    @GetMapping("/active")
    fun getAllActiveUser(): ResponseEntity<List<ResGetUsersDto>>{
        return ResponseEntity.ok(
            masterUserService.findAllActiveUsers()
        )
    }
}