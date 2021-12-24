package com.ifgarces.tomaramosuandes

import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.CsvHandler2
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


class Test_CSVWorker {

    @Test
    fun parseCsv() {
        val tempFile :File = File.createTempFile("test_catalog", ".csv")
        tempFile.writeText(
            text = """PLAN DE ESTUDIOS,NRC,CONECTOR  LIGA,LISTA CRUZADA,MATERIA,CURSO,SECC.,TITULO,CREDITO,LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,INICIO,FIN,SALA,TIPO,PROFESOR
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,08:30 -10:20,,,08:30 -10:20,,,2/3/2022,22/06/2022,R-14,CLAS,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,14:30 -16:20,,,,,,7/3/2022,22/06/2022,,AYUD,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,,,,19:30 -21:20,,,24/03/2022,24/03/2022,,PRBA 1,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,,,,19:30 -21:20,,,24/03/2022,24/03/2022,,PRBA 1,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,,08:30 -12:20,,,,,2/3/2022,22/06/2022,,CLAS,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,,09:30 -11:20,,,,,28/06/2022,28/06/2022,,EXAM,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,,,15:30 -17:20,,,,7/3/2022,22/06/2022,,AYUD,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,08:30 -10:20,,,,,,2/3/2022,22/06/2022,,CLAS,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,14:30 -16:20,,,,,,7/3/2022,22/06/2022,,AYUD,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3789,,,ING,1100,1,ALGEBRA E INTR. AL CALCULO,10,19:30 -21:20,,,,,,11/4/2022,11/4/2022,,PRBA 2,PETERS/RODRIGUEZ EDUARDO FABIAN
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,19:30 -21:20,,,,,,11/4/2022,11/4/2022,,PRBA 2,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,,08:30 -12:20,,,,,2/3/2022,22/06/2022,R-11,CLAS,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,,09:30 -11:20,,,,,28/06/2022,28/06/2022,,EXAM,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,,,15:30 -17:20,,,,7/3/2022,22/06/2022,,AYUD,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3790,,,ING,1100,2,ALGEBRA E INTR. AL CALCULO,10,,,,08:30 -10:20,,,2/3/2022,22/06/2022,R-12,CLAS,SANCHEZ/CANCINO LEONARDO FRANCISCO
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,08:30 -10:20,,,,,,7/3/2022,22/06/2022,B-32,CLAS,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,19:30 -21:20,,,,,,4/4/2022,4/4/2022,,PRBA 1,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,19:30 -21:20,,,,,,4/4/2022,4/4/2022,,PRBA 1,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,19:30 -21:20,,,,,,2/5/2022,2/5/2022,,PRBA 2,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,,15:30 -17:20,,,,,14/03/2022,22/06/2022,,AYUD,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,,,15:30 -17:20,,,,29/06/2022,29/06/2022,,EXAM,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,19:30 -21:20,,,,,,2/5/2022,2/5/2022,,PRBA 2,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,,,,,08:30 -10:20,,7/3/2022,22/06/2022,R-14,CLAS,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,14:30 -16:20,,,,,,7/3/2022,22/06/2022,I201,CLAS,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,,,,19:30 -21:20,,,26/05/2022,26/05/2022,,PRBA 3,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,,,,19:30 -21:20,,,26/05/2022,26/05/2022,,PRBA 3,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3794,,,ING,1201,1,ALGEBRA LINEAL,6,19:30 -21:20,,,,,,13/06/2022,13/06/2022,,PRBA 4,BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,,15:30 -17:20,,,,,14/03/2022,22/06/2022,,AYUD,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,,,15:30 -17:20,,,,7/3/2022,22/06/2022,H-208,CLAS,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,,,11:30 -13:20,,,,29/06/2022,29/06/2022,,EXAM,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3795,,,ING,1201,2,ALGEBRA LINEAL,6,19:30 -21:20,,,,,,13/06/2022,13/06/2022,,PRBA 4,CARRASCO/BRIONES MIGUEL ANGEL 
PE2016,3797,Foo,,ICC,4101,1,ALGORITHMS AND COMPETITIVE PRO,6,,,,15:30 -19:20,,,7/3/2022,22/06/2022,,CLAS,CORREA/VILLANUEVA JAVIER  """,
            charset = Charsets.UTF_8
        )

        val expectedResult :Pair<List<Ramo>, List<RamoEvent>> = Pair(
            listOf(
                Ramo(
                    NRC = 3789,
                    nombre = "ALGEBRA E INTR. AL CALCULO",
                    profesor = "PETERS/RODRIGUEZ EDUARDO FABIAN",
                    créditos = 10,
                    materia = "ING",
                    curso = 1100,
                    sección = "1",
                    planEstudios = "PE2016",
                    conectLiga = "",
                    listaCruzada = ""
                ),
                Ramo(
                    NRC = 3790,
                    nombre = "ALGEBRA E INTR. AL CALCULO",
                    profesor = "SANCHEZ/CANCINO LEONARDO FRANCISCO",
                    créditos = 10,
                    materia = "ING",
                    curso = 1100,
                    sección = "2",
                    planEstudios = "PE2016",
                    conectLiga = "",
                    listaCruzada = ""
                ),
                Ramo(
                    NRC = 3794,
                    nombre = "ALGEBRA LINEAL",
                    profesor = "BASTARRICA/MELGAREJO JOSEFINA ESTEFANIA",
                    créditos = 10,
                    materia = "ING",
                    curso = 1201,
                    sección = "1",
                    planEstudios = "PE2016",
                    conectLiga = "",
                    listaCruzada = ""
                ),
                Ramo(
                    NRC = 3795,
                    nombre = "ALGEBRA LINEAL",
                    profesor = "CARRASCO/BRIONES MIGUEL ANGEL",
                    créditos = 10,
                    materia = "ING",
                    curso = 1201,
                    sección = "2",
                    planEstudios = "PE2016",
                    conectLiga = "",
                    listaCruzada = ""
                ),
                Ramo(
                    NRC = 3797,
                    nombre = "ALGORITHMS AND COMPETITIVE PRO",
                    profesor = "CORREA/VILLANUEVA JAVIER",
                    créditos = 6,
                    materia = "ICC",
                    curso = 4101,
                    sección = "1",
                    planEstudios = "PE2016",
                    conectLiga = "Foo",
                    listaCruzada = ""
                )
            ),
            listOf(
                RamoEvent(
                    ID = 0,
                    ramoNRC = 3789,
                    type = RamoEventType.CLAS,
                    location = "R-14",
                    dayOfWeek = DayOfWeek.MONDAY,
                    startTime = LocalTime.of(8, 30),
                    endTime = LocalTime.of(10, 20),
                    date = LocalDate.of(2022, 3, 2)
                ),
                RamoEvent(
                    ID = 1,
                    ramoNRC = 3789,
                    type = RamoEventType.CLAS,
                    location = "R-14",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    startTime = LocalTime.of(8, 30),
                    endTime = LocalTime.of(10, 20),
                    date = LocalDate.of(2022, 3, 2)
                ),
                RamoEvent(
                    ID = 2,
                    ramoNRC = 3789,
                    type = RamoEventType.AYUD,
                    location = "",
                    dayOfWeek = DayOfWeek.MONDAY,
                    startTime = LocalTime.of(14, 30),
                    endTime = LocalTime.of(16, 20),
                    date = LocalDate.of(2022, 3, 2)
                ),
                RamoEvent(
                    ID = 3,
                    ramoNRC = 3789,
                    type = RamoEventType.PRBA,
                    location = "",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    startTime = LocalTime.of(19, 30),
                    endTime = LocalTime.of(21, 20),
                    date = LocalDate.of(2022, 3, 24)
                ),
                RamoEvent(
                    ID = 4,
                    ramoNRC = 3790,
                    type = RamoEventType.PRBA,
                    location = "",
                    dayOfWeek = DayOfWeek.THURSDAY,
                    startTime = LocalTime.of(19, 30),
                    endTime = LocalTime.of(21, 20),
                    date = LocalDate.of(2022, 3, 24)
                ),
            )
        )

        throw NotImplementedError()

        val gotResult :Pair<List<Ramo>, List<RamoEvent>> = CsvHandler2.parseCsv(tempFile)

        if (expectedResult != gotResult) {
            Assert.fail(
                "[%s] Test failed.\nExpected:\n\t%s\nGot:\n\t%s".format(this::class.simpleName,
                    expectedResult, gotResult
                )
            )
        }
    }
}