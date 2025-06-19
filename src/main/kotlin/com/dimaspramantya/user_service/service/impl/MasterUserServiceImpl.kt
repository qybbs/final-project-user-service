package com.dimaspramantya.user_service.service.impl

import com.dimaspramantya.user_service.domain.constant.Constant
import com.dimaspramantya.user_service.domain.dto.request.ReqLoginDto
import com.dimaspramantya.user_service.domain.dto.request.ReqRegisterDto
import com.dimaspramantya.user_service.domain.dto.request.ReqSetOtpDto
import com.dimaspramantya.user_service.domain.dto.request.ReqUpdateUserDto
import com.dimaspramantya.user_service.domain.dto.request.ReqValidateOtpDto
import com.dimaspramantya.user_service.domain.dto.response.ResGetOtpDto
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
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class MasterUserServiceImpl(
    private val masterUserRepository: MasterUserRepository,
    private val masterRoleRepository: MasterRoleRepository,
    private val jwtUtil: JwtUtil,
    private val bcrypt: BCryptUtil,
    private val httpServletRequest: HttpServletRequest,
    private val stringRedisTemplate: ReactiveStringRedisTemplate
): MasterUserService {
    override fun findAllActiveUsers(): List<ResGetUsersDto> {
        val rawData = masterUserRepository.getAllActiveUser()

        val result = mutableListOf<ResGetUsersDto>()

        rawData.forEach { u ->
            result.add(
                ResGetUsersDto(
                    username = u.username,
                    id = u.id,
                    email = u.email,
                    roleId = u.role?.id,
                    roleName = u.role?.name
                )
            )
        }
        return result
    }

    override fun register(req: ReqRegisterDto): ResGetUsersDto {
        val role = if (req.roleId == null) {
            Optional.empty()
        } else {
            masterRoleRepository.findById(req.roleId)
        }
        if(role.isEmpty && req.roleId != null){
            throw CustomException("Role ${req.roleId} tidak ditemukan", 400)
        }

        val existingUserEmail = masterUserRepository.findFirstByEmail(req.email)
        if (existingUserEmail != null) {
            throw CustomException("Email sudah terdaftar", 400)
        }

        val existingUsername = masterUserRepository
            .findFirstByUsername(req.username)
        if (existingUsername.isPresent) {
            throw CustomException("Username sudah terdaftar", 400)
        }

        val hashPw = bcrypt.hash(req.password)

        val userRaw = MasterUserEntity(
            email = req.email,
            password = hashPw,
            username = req.username,
            role = if (role.isPresent) {
                role.get()
            } else {
                null
            }
        )

        val user = masterUserRepository.save(userRaw)

        return ResGetUsersDto(
            id = user.id,
            email = user.email,
            username = user.username,
            roleId = user.role?.id,
            roleName = user.role?.name
        )
    }

    override fun login(req: ReqLoginDto): ResLoginDto {
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

        return ResLoginDto(role, token)
    }

    override fun setOtp(req: ReqSetOtpDto): ResGetOtpDto {
//        val user = masterUserRepository.findById(userId).orElseThrow {
//            throw CustomException("User with ID $userId not found", 404)
//        }

        val operationString = stringRedisTemplate.opsForValue()

        operationString.set("user-service:user:otp:${req.userId}", req.otpCode, Duration.ofMinutes(15))

        return ResGetOtpDto(
            userId = req.userId,
            otpCode = req.otpCode,
        )
    }

    override fun validateOtp(req: ReqValidateOtpDto): String {
//        val user = masterUserRepository.findById(userId).orElseThrow {
//            throw CustomException("User with ID $userId not found", 404)
//        }

        val operationString = stringRedisTemplate.opsForValue()

        val otpCode = operationString.get("user-service:user:otp:${req.userId}")
            ?: throw CustomException("User with ID ${req.userId} not found in Redis", 404)

        if (!(req.otpCode.equals(otpCode))) {
            throw CustomException("OTP tidak valid", HttpStatus.FORBIDDEN.value())
        }

        return "OTP valid, silakan lanjutkan proses selanjutnya"
    }

    @Cacheable(
        "getUserById",
        key = "{#id}"
    )
    override fun findById(id: Int): ResGetUsersDto {
        val user = masterUserRepository.findById(id).orElseThrow {
            throw CustomException("User dengan id ${id} tidak ditemukan!", 400)
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

    @CacheEvict(
        value = ["getUserById"],
        key = "{#userId}"
    )
    override fun updateUser(
        req: ReqUpdateUserDto,
        userId: Int
    ): ResGetUsersDto {
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
        user.updatedBy = userId.toString()

        val result = masterUserRepository.save(user)

        return ResGetUsersDto(
            id = result.id,
            username = result.username,
            email = result.email
        )
    }
}