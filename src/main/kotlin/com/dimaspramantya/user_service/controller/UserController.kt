package com.dimaspramantya.user_service.controller

import com.dimaspramantya.user_service.domain.dto.request.ReqLoginDto
import com.dimaspramantya.user_service.domain.dto.request.ReqRegisterDto
import com.dimaspramantya.user_service.domain.dto.request.ReqTransferDto
import com.dimaspramantya.user_service.domain.dto.request.ReqUpdateUserDto
import com.dimaspramantya.user_service.domain.dto.response.BaseResponse
import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.domain.dto.response.ResLoginDto
import com.dimaspramantya.user_service.service.MasterUserService
import com.dimaspramantya.user_service.service.ScoreService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val masterUserService: MasterUserService,
    private val scoreService: ScoreService
) {
    @GetMapping("/active")
    fun getAllActiveUser(): ResponseEntity<BaseResponse<List<ResGetUsersDto>>>{
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.findAllActiveUsers()
            )
        )
    }
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody req: ReqRegisterDto
    ): ResponseEntity<BaseResponse<ResGetUsersDto>>{
        return ResponseEntity(
            BaseResponse(
                data = masterUserService.register(req),
                message = "Register sukses"
            ),
            HttpStatus.CREATED
        )
    }
    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: Int
    ): ResponseEntity<BaseResponse<ResGetUsersDto>>{
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.findById(id)
            )
        )
    }

    @PostMapping("/login")
    fun login(
        @RequestBody req: ReqLoginDto
    ): ResponseEntity<BaseResponse<ResLoginDto>> {
        return ResponseEntity(
            BaseResponse(
                data = masterUserService.login(req),
                message = "Login sukses"
            ),
            HttpStatus.OK
        )
    }

    @GetMapping("/test")
    fun testTransaction(
        @RequestParam testcase: String
    ): ResponseEntity<BaseResponse<String>> {
        val response = ResponseEntity.ok(BaseResponse(
            data = scoreService.testIncrementTransaction(testcase)
        ))
        return response
    }

    @PostMapping("/test")
    fun transferScore(
        @RequestBody req: ReqTransferDto
    ): ResponseEntity<BaseResponse<String>> {
        val response = ResponseEntity(
            BaseResponse(
                data = scoreService.transferScore(req.from, req.to, req.score),
                message = "Success Transfer Score"
            ),
            HttpStatus.OK
        )
        return response
    }

    @PutMapping
    fun updateUser(
        @RequestBody req: ReqUpdateUserDto
    ): ResponseEntity<BaseResponse<ResGetUsersDto>>{
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.updateUser(req)
            )
        )
    }
}