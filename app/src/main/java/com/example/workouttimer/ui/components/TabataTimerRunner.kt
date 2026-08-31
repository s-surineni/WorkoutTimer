package com.example.workouttimer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.workouttimer.audio.AudioFeedbackManager
import com.example.workouttimer.audio.NoOpAudioFeedbackManager
import com.example.workouttimer.audio.ToneAudioFeedbackManager
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import com.example.workouttimer.theme.WorkoutTimerTheme
import kotlinx.coroutines.delay

enum class TabataPhase {
    PREPARE,
    WORK,
    REST,
    ROUND_REST,
    COMPLETED
}

/**
 * Full interactive Tabata Workout Timer Runner with distinct, high-contrast colors
 * for Work and Rest intervals, audio feedback cues, and round transitions.
 */
@Composable
fun TabataTimerRunner(
    workout: Workout,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    audioFeedbackManager: AudioFeedbackManager = remember { ToneAudioFeedbackManager() }
) {
    if (workout.exercises.isEmpty()) {
        onDismiss()
        return
    }

    DisposableEffect(audioFeedbackManager) {
        onDispose {
            audioFeedbackManager.release()
        }
    }

    var currentRound by remember { mutableIntStateOf(1) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(TabataPhase.PREPARE) }
    var timeLeft by remember { mutableIntStateOf(3) } // 3s prepare countdown
    var isRunning by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }

    val currentExercise = workout.exercises.getOrNull(currentExerciseIndex) ?: workout.exercises.first()
    val nextExercise = when {
        currentExerciseIndex + 1 < workout.exercises.size -> workout.exercises[currentExerciseIndex + 1]
        currentRound < workout.rounds -> workout.exercises.first()
        else -> null
    }

    val totalPhaseDuration = remember(phase, currentExerciseIndex, currentRound) {
        when (phase) {
            TabataPhase.PREPARE -> 3
            TabataPhase.WORK -> currentExercise.workSeconds.coerceAtLeast(1)
            TabataPhase.REST -> currentExercise.restSeconds.coerceAtLeast(1)
            TabataPhase.ROUND_REST -> workout.restBetweenRoundsSeconds.coerceAtLeast(1)
            TabataPhase.COMPLETED -> 1
        }
    }

    fun playPhaseSound(targetPhase: TabataPhase) {
        if (!isSoundEnabled) return
        when (targetPhase) {
            TabataPhase.WORK -> audioFeedbackManager.playWorkStart()
            TabataPhase.REST, TabataPhase.ROUND_REST -> audioFeedbackManager.playRestStart()
            TabataPhase.COMPLETED -> audioFeedbackManager.playWorkoutComplete()
            TabataPhase.PREPARE -> {}
        }
    }

    // Step logic for moving forward
    fun moveToNext() {
        when (phase) {
            TabataPhase.PREPARE -> {
                phase = TabataPhase.WORK
                timeLeft = currentExercise.workSeconds
                playPhaseSound(TabataPhase.WORK)
            }
            TabataPhase.WORK -> {
                if (currentExercise.restSeconds > 0) {
                    phase = TabataPhase.REST
                    timeLeft = currentExercise.restSeconds
                    playPhaseSound(TabataPhase.REST)
                } else if (currentExerciseIndex + 1 < workout.exercises.size) {
                    currentExerciseIndex += 1
                    phase = TabataPhase.WORK
                    timeLeft = workout.exercises[currentExerciseIndex].workSeconds
                    playPhaseSound(TabataPhase.WORK)
                } else if (currentRound < workout.rounds) {
                    if (workout.restBetweenRoundsSeconds > 0) {
                        phase = TabataPhase.ROUND_REST
                        timeLeft = workout.restBetweenRoundsSeconds
                        playPhaseSound(TabataPhase.ROUND_REST)
                    } else {
                        currentRound += 1
                        currentExerciseIndex = 0
                        phase = TabataPhase.WORK
                        timeLeft = workout.exercises[0].workSeconds
                        playPhaseSound(TabataPhase.WORK)
                    }
                } else {
                    phase = TabataPhase.COMPLETED
                    isRunning = false
                    playPhaseSound(TabataPhase.COMPLETED)
                }
            }
            TabataPhase.REST -> {
                if (currentExerciseIndex + 1 < workout.exercises.size) {
                    currentExerciseIndex += 1
                    phase = TabataPhase.WORK
                    timeLeft = workout.exercises[currentExerciseIndex].workSeconds
                    playPhaseSound(TabataPhase.WORK)
                } else if (currentRound < workout.rounds) {
                    if (workout.restBetweenRoundsSeconds > 0) {
                        phase = TabataPhase.ROUND_REST
                        timeLeft = workout.restBetweenRoundsSeconds
                        playPhaseSound(TabataPhase.ROUND_REST)
                    } else {
                        currentRound += 1
                        currentExerciseIndex = 0
                        phase = TabataPhase.WORK
                        timeLeft = workout.exercises[0].workSeconds
                        playPhaseSound(TabataPhase.WORK)
                    }
                } else {
                    phase = TabataPhase.COMPLETED
                    isRunning = false
                    playPhaseSound(TabataPhase.COMPLETED)
                }
            }
            TabataPhase.ROUND_REST -> {
                currentRound += 1
                currentExerciseIndex = 0
                phase = TabataPhase.WORK
                timeLeft = workout.exercises[0].workSeconds
                playPhaseSound(TabataPhase.WORK)
            }
            TabataPhase.COMPLETED -> {
                isRunning = false
            }
        }
    }

    fun moveToPrevious() {
        if (currentExerciseIndex > 0) {
            currentExerciseIndex -= 1
            phase = TabataPhase.WORK
            timeLeft = workout.exercises[currentExerciseIndex].workSeconds
            playPhaseSound(TabataPhase.WORK)
        } else if (currentRound > 1) {
            currentRound -= 1
            currentExerciseIndex = workout.exercises.lastIndex
            phase = TabataPhase.WORK
            timeLeft = workout.exercises[currentExerciseIndex].workSeconds
            playPhaseSound(TabataPhase.WORK)
        } else {
            phase = TabataPhase.PREPARE
            timeLeft = 3
        }
    }

    fun resetWorkout() {
        currentRound = 1
        currentExerciseIndex = 0
        phase = TabataPhase.PREPARE
        timeLeft = 3
        isRunning = true
    }

    // Timer countdown loop
    LaunchedEffect(isRunning, phase, timeLeft) {
        while (isRunning && phase != TabataPhase.COMPLETED) {
            delay(1000)
            if (timeLeft > 1) {
                timeLeft -= 1
                if (timeLeft in 1..3 && isSoundEnabled) {
                    audioFeedbackManager.playCountdownTick()
                }
            } else {
                moveToNext()
            }
        }
    }

    val progressRatio by remember {
        derivedStateOf {
            if (totalPhaseDuration > 0) {
                1f - (timeLeft.toFloat() / totalPhaseDuration.toFloat())
            } else 0f
        }
    }

    // Bold, distinct, high-contrast color scheme for each phase
    val phaseColor by animateColorAsState(
        targetValue = when (phase) {
            TabataPhase.PREPARE -> Color(0xFFE65100) // Energetic Orange
            TabataPhase.WORK -> Color(0xFF2E7D32) // Bold Emerald Green
            TabataPhase.REST -> Color(0xFF1565C0) // Cool Ocean Blue
            TabataPhase.ROUND_REST -> Color(0xFF6A1B9A) // Deep Royal Purple
            TabataPhase.COMPLETED -> Color(0xFF2E7D32) // Victory Green
        },
        animationSpec = tween(durationMillis = 350),
        label = "phaseColor"
    )

    val cardContainerColor by animateColorAsState(
        targetValue = when (phase) {
            TabataPhase.PREPARE -> Color(0xFFFFF3E0) // Light Warm Orange Container
            TabataPhase.WORK -> Color(0xFFE8F5E9) // Light Crisp Green Container
            TabataPhase.REST -> Color(0xFFE3F2FD) // Light Refreshing Blue Container
            TabataPhase.ROUND_REST -> Color(0xFFF3E5F5) // Light Lavender Container
            TabataPhase.COMPLETED -> Color(0xFFE8F5E9) // Light Green Container
        },
        animationSpec = tween(durationMillis = 350),
        label = "cardContainerColor"
    )

    val onCardColor by animateColorAsState(
        targetValue = when (phase) {
            TabataPhase.PREPARE -> Color(0xFF4E1D00)
            TabataPhase.WORK -> Color(0xFF0F3D17)
            TabataPhase.REST -> Color(0xFF0D3360)
            TabataPhase.ROUND_REST -> Color(0xFF380E54)
            TabataPhase.COMPLETED -> Color(0xFF0F3D17)
        },
        animationSpec = tween(durationMillis = 350),
        label = "onCardColor"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header: Routine title, Sound Toggle & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Round $currentRound of ${workout.rounds} • Exercise ${currentExerciseIndex + 1} of ${workout.exercises.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSoundEnabled = !isSoundEnabled }) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (isSoundEnabled) "Mute Sound" else "Unmute Sound"
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Timer")
                        }
                    }
                }

                // Middle: Phase card & countdown clock with distinct Work/Rest color theme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Phase badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(phaseColor)
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = when (phase) {
                                    TabataPhase.PREPARE -> "GET READY"
                                    TabataPhase.WORK -> "WORK"
                                    TabataPhase.REST -> "REST"
                                    TabataPhase.ROUND_REST -> "ROUND REST"
                                    TabataPhase.COMPLETED -> "FINISHED!"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Exercise Name
                        if (phase == TabataPhase.COMPLETED) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = phaseColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Workout Complete!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = onCardColor,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = if (phase == TabataPhase.ROUND_REST) "Catch Your Breath" else currentExercise.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = onCardColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large Seconds Display
                            Text(
                                text = "$timeLeft",
                                fontSize = 88.sp,
                                fontWeight = FontWeight.Black,
                                color = phaseColor,
                                lineHeight = 92.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phase Progress
                        LinearProgressIndicator(
                            progress = { progressRatio.coerceIn(0f, 1f) },
                            color = phaseColor,
                            trackColor = phaseColor.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next exercise preview
                        if (nextExercise != null && phase != TabataPhase.COMPLETED) {
                            Text(
                                text = "Up Next: ${nextExercise.name} (${nextExercise.workSeconds}s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onCardColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Bottom Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { moveToPrevious() },
                        enabled = phase != TabataPhase.COMPLETED
                    ) {
                        Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Previous Exercise")
                    }

                    FilledIconButton(
                        onClick = {
                            if (phase == TabataPhase.COMPLETED) {
                                resetWorkout()
                            } else {
                                isRunning = !isRunning
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = phaseColor)
                    ) {
                        Icon(
                            imageVector = when {
                                phase == TabataPhase.COMPLETED -> Icons.Default.Replay
                                isRunning -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = if (isRunning) "Pause" else "Play",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { moveToNext() },
                        enabled = phase != TabataPhase.COMPLETED
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Skip Exercise")
                    }

                    IconButton(onClick = { resetWorkout() }) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset Workout")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TabataTimerRunnerPreview() {
    WorkoutTimerTheme {
        TabataTimerRunner(
            workout = Workout(
                title = "Preview Tabata",
                rounds = 2,
                restBetweenRoundsSeconds = 30,
                exercises = listOf(
                    Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
                    Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10)
                )
            ),
            onDismiss = {},
            audioFeedbackManager = NoOpAudioFeedbackManager()
        )
    }
}
