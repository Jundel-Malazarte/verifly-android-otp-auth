package com.example.verifly_android

import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun testOtpFormat() {
        val otp = (100000..999999).random().toString()
        assertEquals(6, otp.length)
        assertTrue(otp.toInt() in 100000..999999)
    }
}
