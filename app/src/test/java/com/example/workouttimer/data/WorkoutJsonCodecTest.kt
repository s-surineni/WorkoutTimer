package com.example.workouttimer.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Test

class WorkoutJsonCodecTest {

    private val sampleWorkout = Workout(
        id = "workout-123",
        title = "Leg Destroyer",
        rounds = 3,
        restBetweenRoundsSeconds = 45,
        exercises = listOf(
            Exercise(id = "e1", name = "Jump Squats", workSeconds = 30, restSeconds = 15),
            Exercise(id = "e2", name = "Lunges", workSeconds = 30, restSeconds = 15)
        )
    )

    @Test
    fun encodeAndDecode_singleWorkout_roundTripMatchesData() {
        val json = WorkoutJsonCodec.encodeWorkout(sampleWorkout)
        val result = WorkoutJsonCodec.decodeWorkout(json, generateNewId = false)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        assertEquals(sampleWorkout.id, decoded.id)
        assertEquals("Leg Destroyer", decoded.title)
        assertEquals(3, decoded.rounds)
        assertEquals(45, decoded.restBetweenRoundsSeconds)
        assertEquals(2, decoded.exercises.size)
        assertEquals("Jump Squats", decoded.exercises[0].name)
    }

    @Test
    fun decodeWorkout_withGenerateNewId_createsDistinctIds() {
        val json = WorkoutJsonCodec.encodeWorkout(sampleWorkout)
        val result = WorkoutJsonCodec.decodeWorkout(json, generateNewId = true)

        assertTrue(result.isSuccess)
        val decoded = result.getOrThrow()
        assertNotSame(sampleWorkout.id, decoded.id)
        assertFalse(decoded.id == sampleWorkout.id)
        assertFalse(decoded.exercises[0].id == sampleWorkout.exercises[0].id)
    }

    @Test
    fun encodeAndDecode_workoutsList_roundTripSucceeds() {
        val workouts = listOf(
            sampleWorkout,
            Workout(
                id = "w2",
                title = "Abs Burner",
                rounds = 2,
                exercises = listOf(Exercise(name = "Plank", workSeconds = 40, restSeconds = 20))
            )
        )

        val json = WorkoutJsonCodec.encodeWorkouts(workouts)
        val result = WorkoutJsonCodec.decodeWorkouts(json, generateNewIds = false)

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(2, list.size)
        assertEquals("Leg Destroyer", list[0].title)
        assertEquals("Abs Burner", list[1].title)
    }

    @Test
    fun decodeWorkout_fromChatMessageWithHeader_extractsAndDecodesSuccessfully() {
        val chatMessage = """
            Hey check out this workout!
            Tabata Routine: Leg Destroyer
            3 Rounds • 2 Exercises
            
            JSON:
            ${WorkoutJsonCodec.encodeWorkout(sampleWorkout)}
            
            Let's do this tomorrow morning!
        """.trimIndent()

        val result = WorkoutJsonCodec.decodeWorkout(chatMessage)
        assertTrue(result.isSuccess)
        val workout = result.getOrThrow()
        assertEquals("Leg Destroyer", workout.title)
        assertEquals(2, workout.exercises.size)
    }

    @Test
    fun decodeWorkout_fromMarkdownCodeFence_extractsAndDecodesSuccessfully() {
        val markdownText = """
            Here is the routine definition:
            ```json
            ${WorkoutJsonCodec.encodeWorkout(sampleWorkout)}
            ```
        """.trimIndent()

        val result = WorkoutJsonCodec.decodeWorkout(markdownText)
        assertTrue(result.isSuccess)
        val workout = result.getOrThrow()
        assertEquals("Leg Destroyer", workout.title)
    }

    @Test
    fun presetRoutines_allValidAndDecodable() {
        assertTrue(PresetRoutines.allPresets.isNotEmpty())
        PresetRoutines.allPresets.forEach { preset ->
            assertTrue(preset.title.isNotBlank())
            assertTrue(preset.rounds > 0)
            assertTrue(preset.exercises.isNotEmpty())
            assertTrue(preset.totalDurationSeconds > 0)
        }
    }

    @Test
    fun decodeWorkout_invalidJson_returnsFailure() {
        val malformedJson = "{ invalid json content }"
        val result = WorkoutJsonCodec.decodeWorkout(malformedJson)

        assertTrue(result.isFailure)
    }

    @Test
    fun decodeWorkout_emptyTitle_returnsFailure() {
        val invalidWorkoutJson = """
            {
                "id": "1",
                "title": "   ",
                "rounds": 2,
                "restBetweenRoundsSeconds": 30,
                "exercises": [
                    { "name": "Push Ups", "workSeconds": 20, "restSeconds": 10 }
                ]
            }
        """.trimIndent()

        val result = WorkoutJsonCodec.decodeWorkout(invalidWorkoutJson)
        assertTrue(result.isFailure)
    }

    @Test
    fun decodeWorkout_emptyExercises_returnsFailure() {
        val invalidWorkoutJson = """
            {
                "id": "1",
                "title": "Empty Workout",
                "rounds": 2,
                "restBetweenRoundsSeconds": 30,
                "exercises": []
            }
        """.trimIndent()

        val result = WorkoutJsonCodec.decodeWorkout(invalidWorkoutJson)
        assertTrue(result.isFailure)
    }
}
