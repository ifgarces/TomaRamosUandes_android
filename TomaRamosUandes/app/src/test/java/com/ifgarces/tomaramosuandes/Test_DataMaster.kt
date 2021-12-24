package com.ifgarces.tomaramosuandes

import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import org.junit.Assert
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


class Test_DataMaster {

//    private val errorMessageFormat :String =
//        "[${this::class.simpleName}] Error in #%d test of %s.\n" +
//            "Expected: \"%s\"\n" +
//            "Got: \"%s\"\n"
//
//    @Test
//    fun areEventsConflicted() {
//        data class Ttest(val param1 :RamoEvent, val param2 :RamoEvent, val expectedOut :Boolean?)
//
//        // non-evaluations. Conflictive: ABC
//        val eventA :RamoEvent = RamoEvent(
//            ID = 956,
//            ramoNRC = 10221,
//            type = RamoEventType.CLAS,
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            startTime = LocalTime.of(12, 30),
//            endTime = LocalTime.of(15, 20),
//            date = null
//        )
//        val eventB :RamoEvent = RamoEvent(
//            ID = 55,
//            ramoNRC = 202,
//            type = RamoEventType.CLAS,
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            startTime = LocalTime.of(13, 30),
//            endTime = LocalTime.of(14, 20),
//            date = null
//        )
//        val eventC :RamoEvent = RamoEvent(
//            ID = 55,
//            ramoNRC = 202,
//            type = RamoEventType.CLAS,
//            dayOfWeek = DayOfWeek.WEDNESDAY,
//            startTime = LocalTime.of(13, 45),
//            endTime = LocalTime.of(14, 0),
//            date = null
//        )
//        val eventD :RamoEvent = RamoEvent(
//            ID = 55,
//            ramoNRC = 202,
//            type = RamoEventType.CLAS,
//            dayOfWeek = DayOfWeek.FRIDAY,
//            startTime = LocalTime.of(12, 30),
//            endTime = LocalTime.of(15, 20),
//            date = null
//        )
//
//        // evaluations. Conflictive: ABC
//        val evalA :RamoEvent = RamoEvent(
//            ID = 1,
//            ramoNRC = 202,
//            type = RamoEventType.PRBA,
//            dayOfWeek = DayOfWeek.SATURDAY,
//            startTime = LocalTime.of(9, 30),
//            endTime = LocalTime.of(12, 0),
//            date = LocalDate.of(2020, 10, 10)
//        )
//        val evalB :RamoEvent = RamoEvent(
//            ID = 8,
//            ramoNRC = 589,
//            type = RamoEventType.PRBA,
//            dayOfWeek = DayOfWeek.SATURDAY,
//            startTime = LocalTime.of(9, 30),
//            endTime = LocalTime.of(12, 0),
//            date = LocalDate.of(2020, 10, 10)
//        )
//        val evalC :RamoEvent = RamoEvent(
//            ID = 8,
//            ramoNRC = 589,
//            type = RamoEventType.EXAM,
//            dayOfWeek = DayOfWeek.SATURDAY,
//            startTime = LocalTime.of(11, 30),
//            endTime = LocalTime.of(15, 20),
//            date = LocalDate.of(2020, 10, 10)
//        )
//        val evalD :RamoEvent = RamoEvent(
//            ID = 8,
//            ramoNRC = 589,
//            type = RamoEventType.PRBA,
//            dayOfWeek = DayOfWeek.SATURDAY,
//            startTime = LocalTime.of(8, 30),
//            endTime = LocalTime.of(9, 20),
//            date = LocalDate.of(2020, 10, 10)
//        )
//        val evalE :RamoEvent = RamoEvent(
//            ID = 8,
//            ramoNRC = 589,
//            type = RamoEventType.PRBA,
//            dayOfWeek = DayOfWeek.FRIDAY,
//            startTime = LocalTime.of(11, 30),
//            endTime = LocalTime.of(15, 20),
//            date = LocalDate.of(2020, 10, 9)
//        )
//        val evalInvalid :RamoEvent = RamoEvent(
//            ID = 8,
//            ramoNRC = 589,
//            type = RamoEventType.EXAM,
//            dayOfWeek = DayOfWeek.MONDAY,
//            startTime = LocalTime.of(8, 30),
//            endTime = LocalTime.of(10, 20),
//            date = null
//        )
//
//        val testSet :List<Ttest> = listOf(
//            Ttest(param1=eventA, param2=eventA, expectedOut=true),
//            Ttest(param1=eventA, param2=eventB, expectedOut=true),
//            Ttest(param1=eventB, param2=eventA, expectedOut=true),
//            Ttest(param1=eventA, param2=eventC, expectedOut=true),
//            Ttest(param1=eventC, param2=eventA, expectedOut=true),
//            Ttest(param1=eventB, param2=eventC, expectedOut=true),
//            Ttest(param1=eventD, param2=eventA, expectedOut=false),
//            Ttest(param1=eventD, param2=eventB, expectedOut=false),
//            Ttest(param1=eventC, param2=eventD, expectedOut=false),
//
//            Ttest(param1=eventA, param2=evalA, expectedOut=null),
//
//            Ttest(param1=evalA, param2=evalA, expectedOut=true),
//            Ttest(param1=evalA, param2=evalB, expectedOut=true),
//            Ttest(param1=evalB, param2=evalA, expectedOut=true),
//            Ttest(param1=evalA, param2=evalC, expectedOut=true),
//            Ttest(param1=evalC, param2=evalA, expectedOut=true),
//            Ttest(param1=evalB, param2=evalC, expectedOut=true),
//            Ttest(param1=evalD, param2=evalA, expectedOut=false),
//            Ttest(param1=evalD, param2=evalB, expectedOut=false),
//            Ttest(param1=evalE, param2=evalA, expectedOut=false),
//            Ttest(param1=evalD, param2=evalE, expectedOut=false),
//            Ttest(param1=evalA, param2=evalInvalid, expectedOut=null)
//        )
//        var expected :Boolean?
//        var got :Boolean?
//        testSet.forEachIndexed { k: Int, test :Ttest ->
//            expected = test.expectedOut
//            got = DataMaster.areEventsConflicted(ev1=test.param1, ev2=test.param2)
//            if (expected != got) {
//                Assert.fail(
//                    this.errorMessageFormat.format(k, "DataMaster.areEventsConflicted()", expected, got)
//                )
//            }
//        }
//    }
//
//    @Test
//    fun getEventsByDay() {
//    }
//
//    @Test
//    fun exportICS() {
//    }
}