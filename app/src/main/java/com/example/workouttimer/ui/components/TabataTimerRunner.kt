package com.example.workouttimer.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.workouttimer.audio.AudioFeedbackManager
import com.example.workouttimer.audio.NoOpAudioFeedbackManager
import com.example.workouttimer.audio.ToneAudioFeedbackManager
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import com.example.workouttimer.theme.WorkoutTimerTheme
import kotlinx.coroutines.delay

enum class TabataPhase {
    PREPARE,
    WARMUP,
    WORK,
    REST,
    ROUND_REST,
    COOLDOWN,
    COMPLETED
}

/**
 * Full interactive Tabata Workout Timer Runner with distinct, high-contrast colors
 * for Warm-Up, Work, Rest, Round Rest, and Cool-Down intervals, immersive full-screen display
 * (hiding navigation & status bars), accidental touch locking, audio feedback cues, and round transitions.
 */
@Composable
fun TabataTimerRunner(
    workout: Workout,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    audioFeedbackManager: AudioFeedbackManager = remember { ToneAudioFeedbackManager() },
    onWorkoutComplete: ((workout: Workout, durationSeconds: Int) -> Unit)? = null
) {
    if (workout.exercises.isEmpty()) {
        onDismiss()
        return
    }

    val context = LocalContext.current

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
    var isScreenLocked by remember { mutableStateOf(false) }

    // Intercept back button gestures during workouts
    BackHandler(enabled = true) {
        if (!isScreenLocked) {
            onDismiss()
        }
    }

    val currentExercise = workout.exercises.getOrNull(currentExerciseIndex) ?: workout.exercises.first()
    val nextExercise = when {
        currentExerciseIndex + 1 < workout.exercises.size -> workout.exercises[currentExerciseIndex + 1]
        currentRound < workout.rounds -> workout.exercises.first()
        else -> null
    }

    val totalPhaseDuration = remember(phase, currentExerciseIndex, currentRound) {
        when (phase) {
            TabataPhase.PREPARE -> 3
            TabataPhase.WARMUP -> workout.warmupSeconds.coerceAtLeast(1)
            TabataPhase.WORK -> currentExercise.workSeconds.coerceAtLeast(1)
            TabataPhase.REST -> currentExercise.restSeconds.coerceAtLeast(1)
            TabataPhase.ROUND_REST -> workout.restBetweenRoundsSeconds.coerceAtLeast(1)
            TabataPhase.COOLDOWN -> workout.cooldownSeconds.coerceAtLeast(1)
            TabataPhase.COMPLETED -> 1
        }
    }

    fun playPhaseSound(targetPhase: TabataPhase) {
        if (!isSoundEnabled) return
        when (targetPhase) {
            TabataPhase.WARMUP, TabataPhase.WORK -> audioFeedbackManager.playWorkStart()
            TabataPhase.REST, TabataPhase.ROUND_REST, TabataPhase.COOLDOWN -> audioFeedbackManager.playRestStart()
            TabataPhase.COMPLETED -> audioFeedbackManager.playWorkoutComplete()
            TabataPhase.PREPARE -> {}
        }
    }

    // Step logic for moving forward
    fun moveToNext() {
        when (phase) {
            TabataPhase.PREPARE -> {
                if (workout.warmupSeconds > 0) {
                    phase = TabataPhase.WARMUP
                    timeLeft = workout.warmupSeconds
                    playPhaseSound(TabataPhase.WARMUP)
                } else {
                    phase = TabataPhase.WORK
                    timeLeft = currentExercise.workSeconds
                    playPhaseSound(TabataPhase.WORK)
                }
            }
            TabataPhase.WARMUP -> {
                phase = TabataPhase.WORK
                timeLeft = workout.exercises[0].workSeconds
                playPhaseSound(TabataPhase.WORK)
            }
            TabataPhase.WORK -> {
                val isLastExerciseInRound = currentExerciseIndex + 1 >= workout.exercises.size
                if (!isLastExerciseInRound && currentExercise.restSeconds > 0) {
                    phase = TabataPhase.REST
                    timeLeft = currentExercise.restSeconds
                    playPhaseSound(TabataPhase.REST)
                } else if (!isLastExerciseInRound) {
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
                } else if (workout.cooldownSeconds > 0) {
                    phase = TabataPhase.COOLDOWN
                    timeLeft = workout.cooldownSeconds
                    playPhaseSound(TabataPhase.COOLDOWN)
                } else {
                    phase = TabataPhase.COMPLETED
                    isScreenLocked = false
                    playPhaseSound(TabataPhase.COMPLETED)
                    onWorkoutComplete?.invoke(workout, workout.totalDurationSeconds)
                }
            }
            TabataPhase.REST -> {
                currentExerciseIndex += 1
                phase = TabataPhase.WORK
                timeLeft = workout.exercises[currentExerciseIndex].workSeconds
                playPhaseSound(TabataPhase.WORK)
            }
            TabataPhase.ROUND_REST -> {
                currentRound += 1
                currentExerciseIndex = 0
                phase = TabataPhase.WORK
                timeLeft = workout.exercises[0].workSeconds
                playPhaseSound(TabataPhase.WORK)
            }
            TabataPhase.COOLDOWN -> {
                phase = TabataPhase.COMPLETED
                isScreenLocked = false
                playPhaseSound(TabataPhase.COMPLETED)
                onWorkoutComplete?.invoke(workout, workout.totalDurationSeconds)
            }
            TabataPhase.COMPLETED -> {}
        }
    }

    // Step logic for moving backward
    fun moveToPrevious() {
        when (phase) {
            TabataPhase.PREPARE -> {}
            TabataPhase.WARMUP -> {
                phase = TabataPhase.PREPARE
                timeLeft = 3
            }
            TabataPhase.WORK -> {
                if (currentExerciseIndex > 0) {
                    currentExerciseIndex -= 1
                    phase = TabataPhase.WORK
                    timeLeft = workout.exercises[currentExerciseIndex].workSeconds
                } else if (currentRound > 1) {
                    currentRound -= 1
                    currentExerciseIndex = workout.exercises.size - 1
                    phase = TabataPhase.WORK
                    timeLeft = workout.exercises[currentExerciseIndex].workSeconds
                } else if (workout.warmupSeconds > 0) {
                    phase = TabataPhase.WARMUP
                    timeLeft = workout.warmupSeconds
                } else {
                    phase = TabataPhase.PREPARE
                    timeLeft = 3
                }
            }
            TabataPhase.REST -> {
                phase = TabataPhase.WORK
                timeLeft = currentExercise.workSeconds
            }
            TabataPhase.ROUND_REST -> {
                phase = TabataPhase.WORK
                currentExerciseIndex = workout.exercises.size - 1
                timeLeft = workout.exercises[currentExerciseIndex].workSeconds
            }
            TabataPhase.COOLDOWN -> {
                phase = TabataPhase.WORK
                currentRound = workout.rounds
                currentExerciseIndex = workout.exercises.size - 1
                timeLeft = workout.exercises[currentExerciseIndex].workSeconds
            }
            TabataPhase.COMPLETED -> {
                if (workout.cooldownSeconds > 0) {
                    phase = TabataPhase.COOLDOWN
                    timeLeft = workout.cooldownSeconds
                } else {
                    phase = TabataPhase.WORK
                    currentRound = workout.rounds
                    currentExerciseIndex = workout.exercises.size - 1
                    timeLeft = workout.exercises[currentExerciseIndex].workSeconds
                }
            }
        }
    }

    fun resetWorkout() {
        currentRound = 1
        currentExerciseIndex = 0
        phase = TabataPhase.PREPARE
        timeLeft = 3
        isRunning = true
        isScreenLocked = false
    }

    // Auto countdown ticker
    LaunchedEffect(isRunning, phase, timeLeft) {
        if (!isRunning || phase == TabataPhase.COMPLETED) return@LaunchedEffect

        // Play 3-2-1 beep during countdown or last 3 seconds of any phase
        if (isSoundEnabled && timeLeft in 1..3) {
            audioFeedbackManager.playCountdownTick()
        }

        delay(1000L)
        if (timeLeft > 1) {
            timeLeft -= 1
        } else {
            moveToNext()
        }
    }

    val progressRatio by remember(timeLeft, totalPhaseDuration) {
        derivedStateOf {
            if (totalPhaseDuration > 0) {
                (totalPhaseDuration - timeLeft).toFloat() / totalPhaseDuration.toFloat()
            } else {
                0f
            }
        }
    }

    // Vibrant phase-dependent theme colors for high visibility and accessibility
    val phaseColor by animateColorAsState(
        targetValue = when (phase) {
            TabataPhase.PREPARE -> Color(0xFFE65100) // Deep Energetic Orange
            TabataPhase.WARMUP -> Color(0xFFF57C00) // Warm Sunset Amber
            TabataPhase.WORK -> Color(0xFF1B5E20) // High-contrast Emerald Green
            TabataPhase.REST -> Color(0xFF0277BD) // Vibrant Ocean Blue
            TabataPhase.ROUND_REST -> Color(0xFF6A1B9A) // Royal Deep Purple
            TabataPhase.COOLDOWN -> Color(0xFF00796B) // Cool Refreshing Teal
            TabataPhase.COMPLETED -> Color(0xFF2E7D32) // Satisfying Success Green
        },
        animationSpec = tween(durationMillis = 350),
        label = "phaseColor"
    )

    val cardContainerColor by animateColorAsState(
        targetValue = when (phase) {
            TabataPhase.PREPARE -> Color(0xFFFFF3E0) // Light Warm Orange Container
            TabataPhase.WARMUP -> Color(0xFFFFF8E1) // Light Warm Amber Container
            TabataPhase.WORK -> Color(0xFFE8F5E9) // Light Crisp Green Container
            TabataPhase.REST -> Color(0xFFE3F2FD) // Light Refreshing Blue Container
            TabataPhase.ROUND_REST -> Color(0xFFF3E5F5) // Light Lavender Container
            TabataPhase.COOLDOWN -> Color(0xFFE0F2F1) // Light Teal Container
            TabataPhase.COMPLETED -> Color(0xFFE8F5E9) // Light Green Container
        },
        animationSpec = tween(durationMillis = 350),
        label = "cardContainerColor"
    )

    val onCardColor by animateColorAsState(
        targetValue = when (phase) {
            TabataPhase.PREPARE -> Color(0xFF4E1D00)
            TabataPhase.WARMUP -> Color(0xFF4E2C00)
            TabataPhase.WORK -> Color(0xFF0F3D17)
            TabataPhase.REST -> Color(0xFF0D3360)
            TabataPhase.ROUND_REST -> Color(0xFF380E54)
            TabataPhase.COOLDOWN -> Color(0xFF003830)
            TabataPhase.COMPLETED -> Color(0xFF0F3D17)
        },
        animationSpec = tween(durationMillis = 350),
        label = "onCardColor"
    )

    Dialog(
        onDismissRequest = {
            if (!isScreenLocked) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current

        // Hide navigation bars (Home, Back, Recents) and status bar on the dialog window
        DisposableEffect(view) {
            val dialogWindow = (view.parent as? DialogWindowProvider)?.window
                ?: (context as? Activity)?.window

            dialogWindow?.let { win ->
                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(win, false)
                val insetsController = WindowCompat.getInsetsController(win, win.decorView)
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
            }

            onDispose {
                dialogWindow?.let { win ->
                    win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    val insetsController = WindowCompat.getInsetsController(win, win.decorView)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }

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
                // Top header: Routine title, Lock toggle, Sound toggle & Close button
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
                            text = when (phase) {
                                TabataPhase.WARMUP -> "Warm-Up Phase • Getting Ready"
                                TabataPhase.COOLDOWN -> "Cool-Down Phase • Recovery"
                                TabataPhase.COMPLETED -> "Workout Finished"
                                else -> "Round $currentRound of ${workout.rounds} • Exercise ${currentExerciseIndex + 1} of ${workout.exercises.size}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isScreenLocked = !isScreenLocked },
                            enabled = phase != TabataPhase.COMPLETED
                        ) {
                            Icon(
                                imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isScreenLocked) "Unlock Screen" else "Lock Screen",
                                tint = if (isScreenLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { isSoundEnabled = !isSoundEnabled },
                            enabled = !isScreenLocked
                        ) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (isSoundEnabled) "Mute Sound" else "Unmute Sound"
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isScreenLocked
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Timer")
                        }
                    }
                }

                // Middle: Phase card & countdown clock with distinct Work/Rest/Warmup/Cooldown color theme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
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
                                    TabataPhase.WARMUP -> "WARM-UP"
                                    TabataPhase.WORK -> "WORK"
                                    TabataPhase.REST -> "REST"
                                    TabataPhase.ROUND_REST -> "ROUND REST"
                                    TabataPhase.COOLDOWN -> "COOL-DOWN"
                                    TabataPhase.COMPLETED -> "FINISHED!"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Exercise Name / Phase Title
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
                                text = when (phase) {
                                    TabataPhase.WARMUP -> "Warm-Up & Mobilize"
                                    TabataPhase.ROUND_REST -> "Catch Your Breath"
                                    TabataPhase.COOLDOWN -> "Cool-Down & Stretch"
                                    else -> currentExercise.name
                                },
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
                        if (phase != TabataPhase.COMPLETED) {
                            val isLastExerciseInRound = currentExerciseIndex + 1 >= workout.exercises.size
                            val upNextText = when {
                                phase == TabataPhase.PREPARE && workout.warmupSeconds > 0 -> "Up Next: Warm-Up (${workout.warmupSeconds}s)"
                                phase == TabataPhase.PREPARE -> "Up Next: ${workout.exercises[0].name} (${workout.exercises[0].workSeconds}s)"
                                phase == TabataPhase.WARMUP -> "Up Next: Round 1 • ${workout.exercises[0].name}"
                                isLastExerciseInRound && currentRound < workout.rounds -> {
                                    if (workout.restBetweenRoundsSeconds > 0) {
                                        "Up Next: Round Rest (${workout.restBetweenRoundsSeconds}s)"
                                    } else {
                                        "Up Next: Round ${currentRound + 1} • ${workout.exercises[0].name}"
                                    }
                                }
                                isLastExerciseInRound && currentRound == workout.rounds && workout.cooldownSeconds > 0 -> "Up Next: Cool-Down (${workout.cooldownSeconds}s)"
                                isLastExerciseInRound && currentRound == workout.rounds -> "Final Exercise!"
                                nextExercise != null && phase == TabataPhase.WORK && currentExercise.restSeconds > 0 -> "Up Next: Rest (${currentExercise.restSeconds}s)"
                                nextExercise != null -> "Up Next: ${nextExercise.name} (${nextExercise.workSeconds}s)"
                                else -> null
                            }

                            if (upNextText != null) {
                                Text(
                                    text = upNextText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onCardColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Locked Status Overlay
                AnimatedVisibility(
                    visible = isScreenLocked,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Screen Locked",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = { isScreenLocked = false },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unlock")
                            }
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
                        enabled = phase != TabataPhase.COMPLETED && !isScreenLocked
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
                        enabled = !isScreenLocked,
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
                        enabled = phase != TabataPhase.COMPLETED && !isScreenLocked
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Skip Exercise")
                    }

                    IconButton(
                        onClick = { resetWorkout() },
                        enabled = !isScreenLocked
                    ) {
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
                warmupSeconds = 30,
                cooldownSeconds = 30,
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
