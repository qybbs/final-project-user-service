package com.dimaspramantya.user_service.service

import com.dimaspramantya.user_service.domain.dto.request.ReqRegisterDto
import com.dimaspramantya.user_service.domain.dto.request.ReqLoginDto
import com.dimaspramantya.user_service.domain.dto.request.ReqSetOtpDto
import com.dimaspramantya.user_service.domain.dto.request.ReqUpdateUserDto
import com.dimaspramantya.user_service.domain.dto.request.ReqValidateOtpDto
import com.dimaspramantya.user_service.domain.dto.response.ResGetOtpDto
import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.domain.dto.response.ResLoginDto

interface MasterUserService {
    fun findAllActiveUsers(): List<ResGetUsersDto>
    fun register(req: ReqRegisterDto): ResGetUsersDto
    fun login(req: ReqLoginDto): ResLoginDto
    fun setOtp(req: ReqSetOtpDto): ResGetOtpDto
    fun validateOtp(req: ReqValidateOtpDto): String
    fun findById(id: Int): ResGetUsersDto
    fun findUsersByIds(ids: List<Int>): List<ResGetUsersDto>
    fun updateUser(req: ReqUpdateUserDto, userId: Int): ResGetUsersDto
}