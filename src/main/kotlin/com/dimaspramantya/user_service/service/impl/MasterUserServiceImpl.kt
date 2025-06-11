package com.dimaspramantya.user_service.service.impl

import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.repository.MasterUserRepository
import com.dimaspramantya.user_service.service.MasterUserService
import org.springframework.stereotype.Service

@Service
class MasterUserServiceImpl(
    private val masterUserRepository: MasterUserRepository
): MasterUserService {
    override fun findAllActiveUsers(): List<ResGetUsersDto> {
        val rawData = masterUserRepository.getAllActiveUser()
        val result = mutableListOf<ResGetUsersDto>()
        //GET ALL USER
        rawData.forEach { u ->
            result.add(
                ResGetUsersDto(
                    username = u.username,
                    id = u.id,
                    email = u.email,
                    //jika user memiliki role maka ambil id role
                    //jika user tidak memiliki role maka value null
                    //GET ROLE BY USER.role_id
                    roleId = u.role?.id,
//                    //jika user memilikie role maka ambil name role
//                    roleName = u.role?.name
                )
            )
        }
        return result
    }
}