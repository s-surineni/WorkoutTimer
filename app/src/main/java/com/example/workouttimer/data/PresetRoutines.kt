package com.example.workouttimer.data

/**
 * Curated preset Tabata / HIIT workout routines ready for one-tap import and exploration.
 */
object PresetRoutines {

    val allPresets: List<Workout> = listOf(
        Workout(
            title = "Full Body HIIT Ignition",
            rounds = 4,
            restBetweenRoundsSeconds = 30,
            exercises = listOf(
                Exercise(name = "Burpees", workSeconds = 20, restSeconds = 10),
                Exercise(name = "Mountain Climbers", workSeconds = 20, restSeconds = 10),
                Exercise(name = "Jump Squats", workSeconds = 20, restSeconds = 10),
                Exercise(name = "High Knees", workSeconds = 20, restSeconds = 10)
            )
        ),
        Workout(
            title = "Core & Abs Destroyer",
            rounds = 3,
            restBetweenRoundsSeconds = 30,
            exercises = listOf(
                Exercise(name = "Plank Hold", workSeconds = 30, restSeconds = 15),
                Exercise(name = "Bicycle Crunches", workSeconds = 30, restSeconds = 15),
                Exercise(name = "Russian Twists", workSeconds = 30, restSeconds = 15),
                Exercise(name = "Flutter Kicks", workSeconds = 30, restSeconds = 15)
            )
        ),
        Workout(
            title = "Upper Body Pump",
            rounds = 3,
            restBetweenRoundsSeconds = 45,
            exercises = listOf(
                Exercise(name = "Standard Push Ups", workSeconds = 25, restSeconds = 15),
                Exercise(name = "Pike Push Ups", workSeconds = 25, restSeconds = 15),
                Exercise(name = "Tricep Dips", workSeconds = 25, restSeconds = 15),
                Exercise(name = "Shoulder Taps", workSeconds = 25, restSeconds = 15)
            )
        ),
        Workout(
            title = "Leg Day Burner",
            rounds = 3,
            restBetweenRoundsSeconds = 30,
            exercises = listOf(
                Exercise(name = "Bodyweight Squats", workSeconds = 30, restSeconds = 15),
                Exercise(name = "Alternating Lunges", workSeconds = 30, restSeconds = 15),
                Exercise(name = "Wall Sit", workSeconds = 30, restSeconds = 15),
                Exercise(name = "Calf Raises", workSeconds = 30, restSeconds = 15)
            )
        ),
        Workout(
            title = "Boxing 3-Minute Circuit",
            rounds = 3,
            restBetweenRoundsSeconds = 60,
            exercises = listOf(
                Exercise(name = "Shadow Boxing Jab-Cross", workSeconds = 45, restSeconds = 15),
                Exercise(name = "Duck & Weave Hooks", workSeconds = 45, restSeconds = 15),
                Exercise(name = "Speedbag Flurry", workSeconds = 45, restSeconds = 15),
                Exercise(name = "Burpee Punches", workSeconds = 45, restSeconds = 15)
            )
        )
    )
}

