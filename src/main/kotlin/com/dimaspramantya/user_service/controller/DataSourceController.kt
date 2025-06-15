package com.dimaspramantya.user_service.controller

import com.dimaspramantya.user_service.domain.dto.response.BaseResponse
import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.service.MasterUserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

//API BANTUAN
@RestController
@RequestMapping("/v1/data-source")
class DataSourceController(
    private val masterUserService: MasterUserService
){
    @GetMapping("/users/by-ids")
    fun getUsersByIds(
        @RequestParam(required = true) userIds: List<Int>
    ): ResponseEntity<BaseResponse<List<ResGetUsersDto>>>{
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.findUsersByIds(userIds)
            )
        )
    }
}