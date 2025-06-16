package com.dimaspramantya.user_service.service.impl

import com.dimaspramantya.user_service.domain.constant.Constant
import com.dimaspramantya.user_service.domain.dto.request.ReqLoginDto
import com.dimaspramantya.user_service.domain.dto.request.ReqRegisterDto
import com.dimaspramantya.user_service.domain.dto.request.ReqUpdateUserDto
import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.domain.dto.response.ResLoginDto
import com.dimaspramantya.user_service.domain.entity.MasterUserEntity
import com.dimaspramantya.user_service.exception.CustomException
import com.dimaspramantya.user_service.repository.MasterRoleRepository
import com.dimaspramantya.user_service.repository.MasterUserRepository
import com.dimaspramantya.user_service.service.MasterUserService
import com.dimaspramantya.user_service.util.BCryptUtil
import com.dimaspramantya.user_service.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class MasterUserServiceImpl(
    private val masterUserRepository: MasterUserRepository,
    private val masterRoleRepository: MasterRoleRepository,
    private val jwtUtil: JwtUtil,
    private val bcrypt: BCryptUtil,
    private val httpServletRequest: HttpServletRequest
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

    override fun register(req: ReqRegisterDto): ResGetUsersDto {
        val role = if(req.roleId == null){
            Optional.empty() //berarti optional.IsEmpty bernilai true
            //berbeda dengan null
        }else{
            masterRoleRepository.findById(req.roleId)
        }

        if(role.isEmpty && req.roleId != null){
            throw CustomException("Role ${req.roleId} tidak ditemukan", 400)
        }

        //cek apakah email sudah terdaftar
        val existingUserEmail = masterUserRepository.findFirstByEmail(req.email)
        if(existingUserEmail != null){
            throw CustomException("Email sudah terdaftar", 400)
        }

        val existingUsername = masterUserRepository
            .findFirstByUsername(req.username)

        if(existingUsername.isPresent){
            throw CustomException("Username sudah terdaftar", 400)
        }

        val hashPw = bcrypt.hash(req.password)

        val userRaw = MasterUserEntity(
            email = req.email,
            password = hashPw,
            username = req.username,
            role = if(role.isPresent){
                role.get()
            }else{
                null
            }
        )
        //entity/row dari hasil save di jadikan sebagi return value
        val user = masterUserRepository.save(userRaw)
        return ResGetUsersDto(
            id = user.id,
            email = user.email,
            username = user.username,
            roleId = user.role?.id
        )
    }

    override fun login(req: ReqLoginDto): ResLoginDto {
        //hasilnya user dengan is_delete false dan is_active true
        val userEntityOpt = masterUserRepository.findFirstByUsername(req.username)

        if (userEntityOpt.isEmpty) {
            throw CustomException("Username atau Password salah", 400)
        }

        val userEntity = userEntityOpt.get()

        if (!bcrypt.verify(req.password, userEntity.password)) {
            throw CustomException("Username atau Password salah", 400)
        }

        val role = if (userEntity.role != null) {
            userEntity.role!!.name
        } else {
            "user"
        }

        val token = jwtUtil.generateToken(userEntity.id, role)

        return ResLoginDto(token)
    }

    override fun findById(id: Int): ResGetUsersDto {
        val user = masterUserRepository.findById(id).orElseThrow {
            throw CustomException("User with id ${id} not found!!!", 400)
        }
        return ResGetUsersDto(
            id = user.id,
            email = user.email,
            username = user.username,
            roleId = user.role?.id,
        )
    }

    override fun findUsersByIds(
        ids: List<Int>
    ): List<ResGetUsersDto> {
        val rawData = masterUserRepository.findAllByIds(
            ids
        )
        return rawData.map {
            ResGetUsersDto(
                id = it.id,
                username = it.username,
                email = it.email
            )
        }
    }

    override fun updateUser(req: ReqUpdateUserDto): ResGetUsersDto {
        //ambil user id
        val userId = httpServletRequest.getHeader(Constant.HEADER_USER_ID)
        println("userId $userId")
        val user = masterUserRepository.findById(userId.toInt()).orElseThrow {
            throw CustomException(
                "User id $userId tidak ditemukan",
                HttpStatus.BAD_REQUEST.value()
            )
        }

        val existingUser = masterUserRepository.findFirstByUsername(req.username)
        if(existingUser.isPresent){
            if(existingUser.get().id != user.id){
                throw CustomException(
                    "Username telah terdaftar",
                    HttpStatus.BAD_REQUEST.value()
                )
            }
        }

        val existingUserEmail = masterUserRepository.findFirstByEmail(req.email)
        if(existingUserEmail != null){
            if(existingUserEmail.id != user.id){
                throw CustomException(
                    "Email telah terdaftar",
                    HttpStatus.BAD_REQUEST.value()
                )
            }
        }

        user.email = req.email
        user.username = req.username
        user.updatedBy = userId

        val result = masterUserRepository.save(user)

        return ResGetUsersDto(
            id = result.id,
            username = result.username,
            email = result.email
        )
    }
}