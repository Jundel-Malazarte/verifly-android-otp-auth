package com.example.verifly_android

import com.example.verifly_android.viewmodel.AuthViewModel
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun testOtpFormat() {
        val otp = (100000..999999).random().toString()
        assertEquals(6, otp.length)
        assertTrue(otp.toInt() in 100000..999999)
    }

    @Test
    fun testPasswordValidationRules() {
        // Less than 8 characters
        assertFalse(AuthViewModel.isValidPassword("Pass1@"))

        // No uppercase
        assertFalse(AuthViewModel.isValidPassword("password123@"))

        // No lowercase
        assertFalse(AuthViewModel.isValidPassword("PASSWORD123@"))

        // No digit
        assertFalse(AuthViewModel.isValidPassword("Password@"))

        // No symbol
        assertFalse(AuthViewModel.isValidPassword("Password123"))

        // Valid password (8+ chars, upper, lower, digit, symbol)
        assertTrue(AuthViewModel.isValidPassword("Password@123"))
        assertTrue(AuthViewModel.isValidPassword("V3rifly#2026"))
    }
}
