package com.dimaspramantya.user_service.service

import com.dimaspramantya.user_service.domain.dto.request.ReqRegisterDto
import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto

interface MasterUserService {
    fun findAllActiveUsers(): List<ResGetUsersDto>
    fun register(req: ReqRegisterDto): ResGetUsersDto
}