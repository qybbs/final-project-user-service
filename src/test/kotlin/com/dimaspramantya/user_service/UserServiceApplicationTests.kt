package com.dimaspramantya.user_service

import com.dimaspramantya.user_service.domain.dto.request.ReqRegisterDto
import com.dimaspramantya.user_service.domain.dto.response.ResGetUsersDto
import com.dimaspramantya.user_service.domain.entity.MasterUserEntity
import com.dimaspramantya.user_service.exception.CustomException
import com.dimaspramantya.user_service.repository.MasterRoleRepository
import com.dimaspramantya.user_service.repository.MasterUserRepository
import com.dimaspramantya.user_service.service.impl.MasterUserServiceImpl
import com.dimaspramantya.user_service.util.BCryptUtil
import com.dimaspramantya.user_service.util.JwtUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.apache.hc.core5.http.HttpStatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class UserServiceApplicationTests {
	private val masterUserRepository: MasterUserRepository = mockk()
	private val masterRoleRepository: MasterRoleRepository = mockk()
	private val bcrypt: BCryptUtil = mockk()
	private val httpServletRequest: HttpServletRequest = mockk()
	private val jwtUtil: JwtUtil = mockk()
	private val masterUserService = MasterUserServiceImpl(
		masterUserRepository = masterUserRepository,
		masterRoleRepository = masterRoleRepository,
		bcrypt = bcrypt,
		httpServletRequest = httpServletRequest,
		jwtUtil = jwtUtil
	)

	@Test
	fun init() {
	}

	@Test
	fun `should register successfully`(){
		val req = ReqRegisterDto(
			email = "test@gmail.com",
			username = "test",
			password = "1234"
		)
		val hashedPassword = "some random hash"

		val userRaw = MasterUserEntity(
			username = req.username,
			email = req.email,
			password = hashedPassword
		)

		val userDb = userRaw
		userDb.id = 1

		every { bcrypt.hash(req.password) } returns hashedPassword
		every {masterUserRepository.findFirstByUsername(req.username)} returns Optional.empty()
		every {masterUserRepository.findFirstByEmail(req.email)} returns null
		every {masterUserRepository.save(any())} returns userDb

		val result = masterUserService.register(req)

		Assertions.assertThat(result).isExactlyInstanceOf(ResGetUsersDto::class.java)
		verify {
			bcrypt.hash(req.password)
			masterUserRepository.findFirstByUsername(req.username)
			masterUserRepository.findFirstByEmail(req.email)
			masterUserRepository.save(any())
		}
	}

	@Test
	fun `should throw bad request because duplicate email when register`(){
		val req = ReqRegisterDto(
			email = "test@gmail.com",
			username = "test",
			password = "1234"
		)
		val hashedPassword = "some random hash"

		val userRaw = MasterUserEntity(
			username = req.username,
			email = req.email,
			password = hashedPassword
		)

		val userDb = userRaw
		userDb.id = 1

		val duplicateEmail = MasterUserEntity(
			id = 2,
			email = req.email,
			username = "testtest",
			password = "hashed password"
		)

		every { bcrypt.hash(req.password) } returns hashedPassword
		every {masterUserRepository.findFirstByUsername(req.username)} returns Optional.empty()
		every {masterUserRepository.findFirstByEmail(req.email)} returns duplicateEmail

		val exception = assertThrows<CustomException> {
			masterUserService.register(req)
		}

		Assertions.assertThat(exception.statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST)
		verify(exactly = 0) {masterUserRepository.save(any())}
	}

}
