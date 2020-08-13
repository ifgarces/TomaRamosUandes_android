package com.ifgarces.tomaramosuandes.utils

import org.junit.Assert
import org.junit.Test


class Test_Extensions {

    private data class StrTest(val paramIn :String, val expectedOut :String)
    private val errorMessageFormat :String =
        "[${this::class.simpleName}] Error in #%d test of %s.\n" +
            "Expected: \"%s\"\n" +
            "Got: \"%s\"\n"

    @Test
    fun spanishUpperCase() {
        val testSet :List<StrTest> = listOf(
            StrTest(paramIn = "Álgebra",                expectedOut = "ÁLGEBRA"),
            StrTest(paramIn = "F",                      expectedOut = "F"),
            StrTest(paramIn = "f",                      expectedOut = "F"),
            StrTest(paramIn = "hola",                   expectedOut = "HOLA"),
            StrTest(paramIn = "¿Cómo estás?_#+",        expectedOut = "¿CÓMO ESTÁS?_#+"),
            StrTest(paramIn = "¿CÓmo estás?_#+",        expectedOut = "¿CÓMO ESTÁS?_#+"),
            StrTest(paramIn = "<pingüino; cumpleaños>", expectedOut = "<PINGÜINO; CUMPLEAÑOS>")
        )
        var expected :String
        var got :String
        testSet.forEachIndexed { k: Int, test :StrTest ->
            expected = test.expectedOut
            got = test.paramIn.spanishUpperCase()
            if (expected != got) {
                Assert.fail(
                    this.errorMessageFormat.format(k, "String.spanishUpperCase()", expected, got)
                )
            }
        }
    }

    @Test
    fun spanishLowerCase() {
        val testSet :List<StrTest> = listOf(
            StrTest(paramIn = "Álgebra",                expectedOut = "álgebra"),
            StrTest(paramIn = "F",                      expectedOut = "f"),
            StrTest(paramIn = "f",                      expectedOut = "f"),
            StrTest(paramIn = "HOLA",                   expectedOut = "hola"),
            StrTest(paramIn = "¿CÓMO ESTÁS?_#+",        expectedOut = "¿cómo estás?_#+"),
            StrTest(paramIn = "¿CÓmo estás?_#+",        expectedOut = "¿cómo estás?_#+"),
            StrTest(paramIn = "<PINGÜINO; CUMPLEAÑOS>", expectedOut = "<pingüino; cumpleaños>")
        )
        var expected :String
        var got :String
        testSet.forEachIndexed { k: Int, test :StrTest ->
            expected = test.expectedOut
            got = test.paramIn.spanishLowerCase()
            if (expected != got) {
                Assert.fail(
                    this.errorMessageFormat.format(k, "String.spanishLowerCase()", expected, got)
                )
            }
        }
    }

    @Test
    fun spanishNonAccent() {
        val testSet :List<StrTest> = listOf(
            StrTest(paramIn = "Álgebra",              expectedOut = "Algebra"),
            StrTest(paramIn = "F",                    expectedOut = "F"),
            StrTest(paramIn = "f",                    expectedOut = "f"),
            StrTest(paramIn = "HOLA",                 expectedOut = "HOLA"),
            StrTest(paramIn = "¿CÓMO ESTÁS?_#+",      expectedOut = "¿COMO ESTAS?_#+"),
            StrTest(paramIn = "¿CÓmo estás?_#+",      expectedOut = "¿COmo estas?_#+"),
            StrTest(paramIn = "PINGÜINO; CUMPLEAÑOS", expectedOut = "PINGUINO; CUMPLEAÑOS"),
            StrTest(paramIn = "<á Á ú ÉñÑñ>",         expectedOut = "<a A u EñÑñ>")
        )
        var expected :String
        var got :String
        testSet.forEachIndexed { k: Int, test :StrTest ->
            expected = test.expectedOut
            got = test.paramIn.spanishNonAccent()
            if (expected != got) {
                Assert.fail(
                    this.errorMessageFormat.format(k, "String.spanishNonAccent()", expected, got)
                )
            }
        }
    }

    @Test
    fun multilineFormat() {
        val testSet :List<StrTest> = listOf(
            StrTest(
                paramIn = "Ignacio F. \\\nGarcés",
                expectedOut = "Ignacio F. Garcés"
            ),
            StrTest(
                paramIn = "Ignacio F. \\ \nGarcés",
                expectedOut = "Ignacio F. \\ \nGarcés"
            ),
            StrTest(
                paramIn = "  \tWhatever  ",
                expectedOut = "Whatever"
            ),
            StrTest(
                paramIn = """
                    1)  First element.
                    2)  Second element. \
                        Still second element.
                """,
                expectedOut = "1) First element.\n 2) Second element. Still second element."
            ),
            StrTest(
                paramIn = """1)  First line.
                    2)  Second line. \
                                Still second line.""",
                expectedOut = "1) First line.\n 2) Second line. Still second line."
            ),
            StrTest(
                paramIn = """ [H] Millhouse!!!! \
                             [M] Quéeeee!!! \
                             [H] Dile a Bart que venga aquí!! \
                             [M] Creo que está con Nelson!! \
                             [H] Quién es Nelson??!!          """,
                expectedOut = "[H] Millhouse!!!! [M] Quéeeee!!! [H] Dile a Bart que venga aquí!! [M] Creo que está con Nelson!! [H] Quién es Nelson??!!"
            )
        )
        var expected :String
        var got :String
        testSet.forEachIndexed { k: Int, test :StrTest ->
            expected = test.expectedOut
            got = test.paramIn.multilineFormat()
            if (expected != got) {
                Assert.fail(
                    this.errorMessageFormat.format(k, "String.multilineFormat()", expected, got)
                )
            }
        }
    }
}