package com.ifgarces.tomaramosuandes.utils

import org.junit.Test
import org.junit.Assert


class TestExtensions {
    data class StrTest(val paramIn :String, val expectedOut :String)

    @Test
    fun spanishUpperCase() {
        val testSet :List<StrTest> = listOf(
            StrTest(paramIn = "F",                    expectedOut = "F"),
            StrTest(paramIn = "f",                    expectedOut = "F"),
            StrTest(paramIn = "hola",                 expectedOut = "HOLA"),
            StrTest(paramIn = "¿Cómo estás?_#+",      expectedOut = "¿CÓMO ESTÁS?_#+"),
            StrTest(paramIn = "¿CÓmo estás?_#+",      expectedOut = "¿CÓMO ESTÁS?_#+"),
            StrTest(paramIn = "pingüino; compleaños", expectedOut = "PINGÜINO; CUMPLEAÑOS")
        )
        testSet.forEachIndexed { _: Int, test :StrTest ->
            Assert.assertEquals(test.expectedOut, test.paramIn.spanishUpperCase())
        }
    }

    @Test
    fun spanishLowerCase() {
        val testSet :List<StrTest> = listOf(
            StrTest(paramIn = "F",                    expectedOut = "f"),
            StrTest(paramIn = "f",                    expectedOut = "f"),
            StrTest(paramIn = "HOLA",                 expectedOut = "hola"),
            StrTest(paramIn = "¿CÓMO ESTÁS?_#+",      expectedOut = "¿cómo estás?_#+"),
            StrTest(paramIn = "¿CÓmo estás?_#+",      expectedOut = "¿cómo estás?_#+"),
            StrTest(paramIn = "PINGÜINO; CUMPLEAÑOS", expectedOut = "pingüino; compleaños")
        )
        testSet.forEachIndexed { _: Int, test :StrTest ->
            Assert.assertEquals(test.expectedOut, test.paramIn.spanishLowerCase())
        }
    }

    @Test
    fun spanishNonAccent() {
        val testSet :List<StrTest> = listOf(
            StrTest(paramIn = "F",                    expectedOut = "F"),
            StrTest(paramIn = "f",                    expectedOut = "f"),
            StrTest(paramIn = "HOLA",                 expectedOut = "hola"),
            StrTest(paramIn = "¿CÓMO ESTÁS?_#+",      expectedOut = "¿COMO ESTAS?_#+"),
            StrTest(paramIn = "¿CÓmo estás?_#+",      expectedOut = "¿CÓmo estas?_#+"),
            StrTest(paramIn = "PINGÜINO; CUMPLEAÑOS", expectedOut = "PINGUINO; CUMPLEAÑOS")
        )
        testSet.forEachIndexed { _: Int, test :StrTest ->
            Assert.assertEquals(test.expectedOut, test.paramIn.spanishNonAccent())
        }
    }

    @Test
    fun multilineFormat() {
    }
}