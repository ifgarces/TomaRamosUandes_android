package com.ifgarces.tomaramosuandes.utils

import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime


class Test_CSVWorker {

    private data class Ttest(val paramIn :List<String>, val expectedOut :List<Ramo>?)
    private val errorMessageFormat :String =
        "[${this::class.simpleName}] Error in #%d test of %s.\n" +
                "Expected: \"%s\"\n" +
                "Got: \"%s\"\n"

    @Test
    fun parseCSV() {

        val testSet :List<Ttest> = listOf(
            Ttest(
                paramIn = listOf(""),
                expectedOut = null
            ),
            Ttest(
                paramIn = listOf("ELECTIVOS TEOLOGÍA III,2209,,loquesea,FRM,,1,MUNDO;; TRABAJO Y REDENCIÓN,3,,17:30 - 19:20,,,,,,OLIN,AMADO/FERNÁNDEZ ANTONIO"),
                expectedOut = listOf(
                    Ramo(
                        NRC = 2209,
                        nombre = "MUNDO, TRABAJO Y REDENCIÓN",
                        profesor = "AMADO/FERNÁNDEZ ANTONIO",
                        créditos = 3,
                        materia = "FRM",
                        curso = 0,
                        sección = "1",
                        planEstudios = "ELECTIVOS TEOLOGÍA III",
                        conectLiga = "",
                        listaCruzada = "loquesea",
                        events = listOf(
                            RamoEvent(
                                ID = 0,
                                ramoNRC = 2209,
                                type = 0,
                                dayofWeek = DayOfWeek.TUESDAY,
                                startTime = LocalTime.of(17, 30),
                                endTime = LocalTime.of(19, 20),
                                date = null
                            )
                        )
                    )
                )
            ),
            Ttest(
                paramIn = listOf("ELECTIVOS TEOLOGÍA III,2209q,,loquesea,FRM,,1,MUNDO;; TRABAJO Y REDENCIÓN,3,,17:30 - 19:20,,,,,,OLIN,AMADO/FERNÁNDEZ ANTONIO"),
                expectedOut = null
            ),
            Ttest(
                paramIn = listOf("ELECTIVOS TEOLOGÍA III,2209q,,loquesea,FRM,,1,MUNDO;; TRABAJO Y REDENCIÓN,3,,17:30-25:20,,,,,,OLIN,AMADO/FERNÁNDEZ ANTONIO"),
                expectedOut = null
            ),
            Ttest(
                paramIn = """PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,14:30 - 16:20,,,,,10/08/2020,25/11/2020,AYON, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,14:30 - 16:20,,,,,24/08/2020,24/08/2020,PRON 1, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,14:30 - 16:20,,,,,21/09/2020,21/09/2020,PRON 2, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,14:30 - 16:20,,,,,19/10/2020,19/10/2020,PRON 3, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,14:30 - 16:20,,,,,02/11/2020,02/11/2020,PRON 4, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,14:30 - 16:20,,,,,23/11/2020,23/11/2020,PRON 5, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,,08:30 - 12:20,,,,03/08/2020,25/11/2020,OLIN, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,,09:30 - 11:20,,,,01/12/2020,01/12/2020,EXON, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,,,10:30 - 12:20,,,03/08/2020,25/11/2020,OLIN, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,,,,08:30 - 10:20,,03/08/2020,25/11/2020,OLIN, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,625,,,ING,1100,1,ÁLGEBRA E INTR. AL CÁLCULO,10,,,,,13:30 - 15:20,10/08/2020,25/11/2020,AYON, PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,19:30 - 22:20,,,,,19/10/2020,19/10/2020,PRON 3,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,09:30 - 11:20,,,,,07/12/2020,07/12/2020,EXON,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,15:30 - 17:20,,,,03/08/2020,25/11/2020,OLIN,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,,12:30 - 14:20,,,10/08/2020,25/11/2020,AYON,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,,12:30 - 14:20,,,23/09/2020,23/09/2020,PRON 2,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,,12:30 - 14:20,,,25/11/2020,25/11/2020,PRON 5,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,,,19:30 - 22:20,,05/11/2020,05/11/2020,PRON 4,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,,,,15:30 - 17:20,03/08/2020,25/11/2020,OLIN,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,608,,,ING,1201,1,ÁLGEBRA LINEAL,6,,,,,19:30 - 22:20,21/08/2020,21/08/2020,PRON 1,BALLESTEROS/VERGARA RAIMUNDO TOMÁS
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,19:30 - 22:20,,,,,19/10/2020,19/10/2020,PRON 3,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,09:30 - 11:20,,,,,07/12/2020,07/12/2020,EXON,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,,14:30 - 16:20,,14:30 - 16:20,,03/08/2020,25/11/2020,OLIN,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,,,12:30 - 14:20,,,10/08/2020,25/11/2020,AYON,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,,,12:30 - 14:20,,,23/09/2020,23/09/2020,PRON 2,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,,,12:30 - 14:20,,,25/11/2020,25/11/2020,PRON 5,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,,,,19:30 - 22:20,,05/11/2020,05/11/2020,PRON 4,CARRASCO/BRIONES MIGUEL ÁNGEL 
PE2016,609,,,ING,1201,2,ÁLGEBRA LINEAL,6,,,,,19:30 - 22:20,21/08/2020,21/08/2020,PRON 1,CARRASCO/BRIONES MIGUEL ÁNGEL"""
                    .split("\n"),
                expectedOut = listOf(
                    Ramo(
                        NRC = 0,
                        nombre = "ÁLGEBRA E INTR. AL CÁLCULO",
                        profesor = "PIRELA/PÉREZ CARLOS SANTIAGO + SÁNCHEZ/CANCINO LEONARDO FRANCISCO",
                        créditos = 0,
                        materia = "",
                        curso = 0,
                        sección = "",
                        planEstudios = "",
                        conectLiga = "",
                        listaCruzada = "",
                        events = listOf()
                    ),
                    Ramo(
                        NRC = 0,
                        nombre = "ÁLGEBRA LINEAL",
                        profesor = "BALLESTEROS/VERGARA RAIMUNDO TOMÁS",
                        créditos = 0,
                        materia = "",
                        curso = 0,
                        sección = "",
                        planEstudios = "",
                        conectLiga = "",
                        listaCruzada = "",
                        events = listOf()
                    ),
                    Ramo(
                        NRC = 0,
                        nombre = "ÁLGEBRA LINEAL",
                        profesor = "CARRASCO/BRIONES MIGUEL ÁNGEL",
                        créditos = 0,
                        materia = "",
                        curso = 0,
                        sección = "",
                        planEstudios = "",
                        conectLiga = "",
                        listaCruzada = "",
                        events = listOf()
                    )
                )
            )
        )

        var expected :List<Ramo>?
        var got :List<Ramo>?
        testSet.forEachIndexed { k: Int, test :Ttest ->
            expected = test.expectedOut
            got = CSVWorker.parseCSV(test.paramIn)
            if (expected != got) {
                Assert.fail(
                    this.errorMessageFormat.format(k, "CSVWorker.parseCSV()", expected, got)
                )
            }
        }
    }
}