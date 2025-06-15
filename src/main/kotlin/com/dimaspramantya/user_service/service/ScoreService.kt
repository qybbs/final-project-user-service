package com.dimaspramantya.user_service.service

interface ScoreService {
    fun testIncrementTransaction(testcase: String): String
    fun testSelfInvocationTransaction(testcase: String): String
    fun transferScore(from: String, to: String, score: Int): String
}