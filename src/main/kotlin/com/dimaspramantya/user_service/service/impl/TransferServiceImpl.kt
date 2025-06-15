package com.dimaspramantya.user_service.service.impl

import com.dimaspramantya.user_service.domain.entity.ScoreEntity
import com.dimaspramantya.user_service.exception.CustomException
import com.dimaspramantya.user_service.repository.ScoreRepository
import com.dimaspramantya.user_service.service.TransferService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TransferServiceImpl(
     private val scoreRepository: ScoreRepository
): TransferService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun processTransfer(from: ScoreEntity, to: ScoreEntity, score: Int): String {
        if (from.score!! < score) {
            throw CustomException("Simulated failure for testing", 500)
        }

        from.score = from.score!! - score
        to.score = to.score?.plus(score) ?: score

        scoreRepository.save(from)
        scoreRepository.save(to)

        return "Transaction completed successfully"
    }
}