package com.dimaspramantya.user_service.service.impl

import com.dimaspramantya.user_service.domain.entity.ScoreEntity
import com.dimaspramantya.user_service.exception.CustomException
import com.dimaspramantya.user_service.repository.ScoreRepository
import com.dimaspramantya.user_service.service.ScoreService
import com.dimaspramantya.user_service.service.TransferService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScoreServiceImpl(
    private val transferService: TransferService,
    private val scoreRepository: ScoreRepository
): ScoreService {
    @Transactional
    override fun testIncrementTransaction(testcase: String): String {
        val dataTest: MutableList<ScoreEntity> = mutableListOf()

        for (i in 1..5) {
            val scoreEntity = ScoreEntity(
                name = "Test - $i",
                score = i * 5,
                hasTransferred = false
            )

            dataTest.add(scoreEntity)
        }

        scoreRepository.saveAll(dataTest)

        if (testcase.equals("fail", ignoreCase = true)) {
            throw CustomException("Simulated failure for testing", 500)
        }

        return "Transaction completed successfully"
    }

//    @Transactional
    override fun testSelfInvocationTransaction(testcase: String): String {
        return testIncrementTransaction(testcase)
    }

    @Transactional
    override fun transferScore(from: String, to: String, score: Int): String {
        val fromScoreEntity = scoreRepository.findFirstByName(from)
            .orElseThrow { RuntimeException("From user not found") }

        val toScoreEntity = scoreRepository.findFirstByName(to)
            .orElseThrow { RuntimeException("To user not found") }

        fromScoreEntity.hasTransferred = true
        val fromScoreEntityNew = scoreRepository.saveAndFlush(fromScoreEntity)

        try {
            val fromTransferServcice = transferService.processTransfer(fromScoreEntityNew, toScoreEntity, score)
        } catch (e: Exception) {
            println("Transfer failed: ${e.message}")
        }

//        return fromTransferServcice
        return "Transaction completed successfully"
    }
}