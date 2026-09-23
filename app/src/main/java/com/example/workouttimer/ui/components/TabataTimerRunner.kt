package com.example.workouttimer.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.workouttimer.theme.TimerNumberStyle
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

/** Constants and text formatting helpers for [TabataTimerRunner]. */
object TabataTimerRunnerConstants {
    // Animation labels
    const val ANIM_LABEL_PHASE_COLOR = "phaseColor"
    const val ANIM_LABEL_CARD_CONTAINER_COLOR = "cardContainerColor"
    const val ANIM_LABEL_ON_CARD_COLOR = "onCardColor"
    const val ANIM_LABEL_PULSE_SCALE = "pulseScale"

    // Phase badge labels
    const val LABEL_GET_READY = "GET READY"
    const val LABEL_WARMUP = "WARM-UP"
    const val LABEL_WORK = "WORK"
    const val LABEL_REST = "REST"
    const val LABEL_ROUND_REST = "ROUND REST"
    const val LABEL_COOLDOWN = "COOL-DOWN"
    const val LABEL_FINISHED = "FINISHED!"
    const val LABEL_COMING_UP_NEXT = "COMING UP NEXT"

    // Exercise & Phase display titles
    const val TITLE_WORKOUT_COMPLETE = "Workout Complete!"
    const val TITLE_WARMUP_MOBILIZE = "Warm-Up & Mobilize"
    const val TITLE_REST_RECOVER = "Rest & Recover"
    const val TITLE_CATCH_BREATH = "Catch Your Breath"
    const val TITLE_COOLDOWN_STRETCH = "Cool-Down & Stretch"

    // Subtitles
    const val SUBTITLE_WARMUP_PHASE = "Warm-Up Phase • Getting Ready"
    const val SUBTITLE_COOLDOWN_PHASE = "Cool-Down Phase • Recovery"
    const val SUBTITLE_WORKOUT_FINISHED = "Workout Finished"

    // Screen lock overlay
    const val TEXT_SCREEN_LOCKED = "Screen Locked"
    const val TEXT_UNLOCK = "Unlock"

    // Accessibility content descriptions
    const val CD_UNLOCK_SCREEN = "Unlock Screen"
    const val CD_LOCK_SCREEN = "Lock Screen"
    const val CD_MUTE_SOUND = "Mute Sound"
    const val CD_UNMUTE_SOUND = "Unmute Sound"
    const val CD_CLOSE_TIMER = "Close Timer"
    const val CD_PREVIOUS_EXERCISE = "Previous Exercise"
    const val CD_SKIP_EXERCISE = "Skip Exercise"
    const val CD_RESET_WORKOUT = "Reset Workout"
    const val CD_PAUSE = "Pause"
    const val CD_PLAY = "Play"
    const val CD_INTERVAL_TIMELINE = "Interval sequence timeline"

    // Progress indicators
    const val LABEL_ROUND = "Round"
    const val LABEL_EXERCISE = "Exercise"
    const val LABEL_PHASE = "Phase"
    const val LABEL_STATUS = "Status"

    // Up next indicators
    const val TEXT_FINAL_EXERCISE = "Final Exercise!"

    // Dynamic text helpers
    fun formatRatio(current: Int, total: Int): String =
        "$current of $total"

    fun formatRoundProgress(currentRound: Int, totalRounds: Int): String =
        "Round $currentRound of $totalRounds"

    fun formatExerciseProgress(currentExerciseIndex: Int, totalExercises: Int): String =
        "Exercise ${currentExerciseIndex + 1} of $totalExercises"

    fun subtitleGettingReady(totalRounds: Int): String =
        "Round 1 of $totalRounds • Getting Ready"

    fun subtitleRoundProgress(currentRound: Int, totalRounds: Int, currentExerciseIndex: Int, totalExercises: Int): String =
        "Round $currentRound of $totalRounds • Exercise ${currentExerciseIndex + 1} of $totalExercises"

    fun upNextRound(round: Int, exerciseName: String): String =
        "Up Next: Round $round • $exerciseName"

    fun upNextRoundRest(seconds: Int): String =
        "Up Next: Round Rest (${seconds}s)"

    fun upNextCoolDown(seconds: Int): String =
        "Up Next: Cool-Down (${seconds}s)"

    fun upNextRest(seconds: Int): String =
        "Up Next: Rest (${seconds}s)"

    fun upNextExercise(name: String, seconds: Int): String =
        "Up Next: $name (${seconds}s)"

    fun phaseTotalDurationLabel(seconds: Int): String =
        "/ ${seconds}s"

    fun intervalSegmentDescription(index: Int, name: String, isCompleted: Boolean, isActive: Boolean): String = when {
        isCompleted -> "Exercise ${index + 1}: $name (Completed)"
        isActive -> "Exercise ${index + 1}: $name (Current Interval)"
        else -> "Exercise ${index + 1}: $name (Upcoming)"
    }
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
    val haptic = LocalHapticFeedback.current

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

        // Play 3-2-1 beep and trigger tactile haptic tick during countdown or last 3 seconds of any phase
        if (timeLeft in 1..3) {
            if (isSoundEnabled) {
                audioFeedbackManager.playCountdownTick()
            }
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }

        delay(1000L)
        if (timeLeft > 1) {
            timeLeft -= 1
        } else {
            moveToNext()
        }
    }

    // Haptic feedback on interval phase transitions
    LaunchedEffect(phase) {
        if (phase != TabataPhase.PREPARE) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val isUrgentTick = isRunning && timeLeft in 1..3 && phase != TabataPhase.COMPLETED
    val pulseScale by animateFloatAsState(
        targetValue = if (isUrgentTick) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = TabataTimerRunnerConstants.ANIM_LABEL_PULSE_SCALE
    )

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
        label = TabataTimerRunnerConstants.ANIM_LABEL_PHASE_COLOR
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
        label = TabataTimerRunnerConstants.ANIM_LABEL_CARD_CONTAINER_COLOR
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
        label = TabataTimerRunnerConstants.ANIM_LABEL_ON_CARD_COLOR
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
                // Top Section: Routine title, Lock toggle, Sound toggle, Close button, and Progress Status Chips
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isScreenLocked = !isScreenLocked },
                                enabled = phase != TabataPhase.COMPLETED
                            ) {
                                Icon(
                                    imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (isScreenLocked) TabataTimerRunnerConstants.CD_UNLOCK_SCREEN else TabataTimerRunnerConstants.CD_LOCK_SCREEN,
                                    tint = if (isScreenLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { isSoundEnabled = !isSoundEnabled },
                                enabled = !isScreenLocked
                            ) {
                                Icon(
                                    imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = if (isSoundEnabled) TabataTimerRunnerConstants.CD_MUTE_SOUND else TabataTimerRunnerConstants.CD_UNMUTE_SOUND
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                enabled = !isScreenLocked
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = TabataTimerRunnerConstants.CD_CLOSE_TIMER
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Status Row: dedicated Round and Exercise position chips
                    WorkoutProgressStatusRow(
                        phase = phase,
                        currentRound = currentRound,
                        totalRounds = workout.rounds,
                        currentExerciseIndex = currentExerciseIndex,
                        totalExercises = workout.exercises.size,
                        warmupSeconds = workout.warmupSeconds
                    )
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
                                    TabataPhase.PREPARE -> TabataTimerRunnerConstants.LABEL_GET_READY
                                    TabataPhase.WARMUP -> TabataTimerRunnerConstants.LABEL_WARMUP
                                    TabataPhase.WORK -> TabataTimerRunnerConstants.LABEL_WORK
                                    TabataPhase.REST -> TabataTimerRunnerConstants.LABEL_REST
                                    TabataPhase.ROUND_REST -> TabataTimerRunnerConstants.LABEL_ROUND_REST
                                    TabataPhase.COOLDOWN -> TabataTimerRunnerConstants.LABEL_COOLDOWN
                                    TabataPhase.COMPLETED -> TabataTimerRunnerConstants.LABEL_FINISHED
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Exercise Name / Phase Title & Circular Countdown Display
                        if (phase == TabataPhase.COMPLETED) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = phaseColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = TabataTimerRunnerConstants.TITLE_WORKOUT_COMPLETE,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = onCardColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interval sequence timeline
                            IntervalTimelineRow(
                                totalExercises = workout.exercises.size,
                                currentExerciseIndex = currentExerciseIndex,
                                phase = phase,
                                exercises = workout.exercises,
                                phaseColor = phaseColor,
                                onCardColor = onCardColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = when (phase) {
                                    TabataPhase.PREPARE -> if (workout.warmupSeconds > 0) TabataTimerRunnerConstants.TITLE_WARMUP_MOBILIZE else currentExercise.name
                                    TabataPhase.WARMUP -> TabataTimerRunnerConstants.TITLE_WARMUP_MOBILIZE
                                    TabataPhase.REST -> TabataTimerRunnerConstants.TITLE_REST_RECOVER
                                    TabataPhase.ROUND_REST -> TabataTimerRunnerConstants.TITLE_CATCH_BREATH
                                    TabataPhase.COOLDOWN -> TabataTimerRunnerConstants.TITLE_COOLDOWN_STRETCH
                                    else -> currentExercise.name
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = onCardColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Circular Radial Gauge & Seconds Countdown Display with Tabular Figures
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { progressRatio.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxSize(),
                                    color = phaseColor,
                                    trackColor = phaseColor.copy(alpha = 0.18f),
                                    strokeWidth = 12.dp,
                                    strokeCap = StrokeCap.Round
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                ) {
                                    Text(
                                        text = "$timeLeft",
                                        style = TimerNumberStyle,
                                        color = phaseColor,
                                        textAlign = TextAlign.Center
                                    )
                                    if (totalPhaseDuration > 0) {
                                        Text(
                                            text = TabataTimerRunnerConstants.phaseTotalDurationLabel(totalPhaseDuration),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = onCardColor.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Next exercise preview
                        if (phase != TabataPhase.COMPLETED) {
                            val isLastExerciseInRound = currentExerciseIndex + 1 >= workout.exercises.size
                            val upNextText = when {
                                phase == TabataPhase.PREPARE && workout.warmupSeconds > 0 ->
                                    TabataTimerRunnerConstants.upNextRound(1, workout.exercises[0].name)
                                phase == TabataPhase.PREPARE -> null
                                phase == TabataPhase.WARMUP ->
                                    TabataTimerRunnerConstants.upNextRound(1, workout.exercises[0].name)
                                isLastExerciseInRound && currentRound < workout.rounds -> {
                                    if (phase == TabataPhase.WORK && workout.restBetweenRoundsSeconds > 0) {
                                        TabataTimerRunnerConstants.upNextRoundRest(workout.restBetweenRoundsSeconds)
                                    } else {
                                        TabataTimerRunnerConstants.upNextRound(currentRound + 1, workout.exercises[0].name)
                                    }
                                }
                                isLastExerciseInRound && currentRound == workout.rounds && workout.cooldownSeconds > 0 ->
                                    TabataTimerRunnerConstants.upNextCoolDown(workout.cooldownSeconds)
                                isLastExerciseInRound && currentRound == workout.rounds ->
                                    TabataTimerRunnerConstants.TEXT_FINAL_EXERCISE
                                nextExercise != null && phase == TabataPhase.WORK && currentExercise.restSeconds > 0 ->
                                    TabataTimerRunnerConstants.upNextRest(currentExercise.restSeconds)
                                phase == TabataPhase.REST && nextExercise != null ->
                                    TabataTimerRunnerConstants.upNextExercise(nextExercise.name, nextExercise.workSeconds)
                                phase == TabataPhase.REST -> null
                                nextExercise != null ->
                                    TabataTimerRunnerConstants.upNextExercise(nextExercise.name, nextExercise.workSeconds)
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
                                    text = TabataTimerRunnerConstants.TEXT_SCREEN_LOCKED,
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
                                Text(TabataTimerRunnerConstants.TEXT_UNLOCK)
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
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = TabataTimerRunnerConstants.CD_PREVIOUS_EXERCISE
                        )
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
                            contentDescription = if (isRunning) TabataTimerRunnerConstants.CD_PAUSE else TabataTimerRunnerConstants.CD_PLAY,
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { moveToNext() },
                        enabled = phase != TabataPhase.COMPLETED && !isScreenLocked
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = TabataTimerRunnerConstants.CD_SKIP_EXERCISE
                        )
                    }

                    IconButton(
                        onClick = { resetWorkout() },
                        enabled = !isScreenLocked
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = TabataTimerRunnerConstants.CD_RESET_WORKOUT
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual capsule / dot indicators showing progress through the exercises in the current round.
 */
@Composable
fun IntervalTimelineRow(
    totalExercises: Int,
    currentExerciseIndex: Int,
    phase: TabataPhase,
    exercises: List<Exercise>,
    phaseColor: Color,
    onCardColor: Color,
    modifier: Modifier = Modifier
) {
    if (totalExercises <= 1 && phase != TabataPhase.WARMUP && phase != TabataPhase.COOLDOWN) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = TabataTimerRunnerConstants.CD_INTERVAL_TIMELINE },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        exercises.forEachIndexed { index, exercise ->
            val isCompleted = when {
                phase == TabataPhase.COMPLETED || phase == TabataPhase.COOLDOWN -> true
                phase == TabataPhase.ROUND_REST -> true
                phase == TabataPhase.WARMUP || phase == TabataPhase.PREPARE -> false
                else -> index < currentExerciseIndex
            }
            val isActive = when {
                phase == TabataPhase.WORK || phase == TabataPhase.REST -> index == currentExerciseIndex
                else -> false
            }

            val segmentDescription = TabataTimerRunnerConstants.intervalSegmentDescription(
                index = index,
                name = exercise.name,
                isCompleted = isCompleted,
                isActive = isActive
            )

            val segmentWidth by animateDpAsState(
                targetValue = if (isActive) 24.dp else 10.dp,
                label = "segmentWidth"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(segmentWidth)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            isActive -> phaseColor
                            isCompleted -> phaseColor.copy(alpha = 0.55f)
                            else -> onCardColor.copy(alpha = 0.2f)
                        }
                    )
                    .semantics { contentDescription = segmentDescription }
            )
        }
    }
}

/**
 * Polished status chips displaying current round and current exercise position
 * (or active phase indicators during Warm-Up, Cool-Down, and Completion).
 */
@Composable
fun WorkoutProgressStatusRow(
    phase: TabataPhase,
    currentRound: Int,
    totalRounds: Int,
    currentExerciseIndex: Int,
    totalExercises: Int,
    warmupSeconds: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val showExercisePosition = when (phase) {
            TabataPhase.PREPARE -> warmupSeconds == 0
            TabataPhase.WORK, TabataPhase.REST -> true
            else -> false
        }

        when {
            showExercisePosition -> {
                ProgressBadge(
                    icon = Icons.Default.Repeat,
                    label = TabataTimerRunnerConstants.LABEL_ROUND,
                    value = TabataTimerRunnerConstants.formatRatio(currentRound, totalRounds),
                    contentDescription = TabataTimerRunnerConstants.formatRoundProgress(currentRound, totalRounds)
                )

                ProgressBadge(
                    icon = Icons.Default.FitnessCenter,
                    label = TabataTimerRunnerConstants.LABEL_EXERCISE,
                    value = TabataTimerRunnerConstants.formatRatio(currentExerciseIndex + 1, totalExercises),
                    contentDescription = TabataTimerRunnerConstants.formatExerciseProgress(currentExerciseIndex, totalExercises)
                )
            }
            phase == TabataPhase.ROUND_REST -> {
                ProgressBadge(
                    icon = Icons.Default.Repeat,
                    label = TabataTimerRunnerConstants.LABEL_ROUND,
                    value = TabataTimerRunnerConstants.formatRatio(currentRound, totalRounds),
                    contentDescription = TabataTimerRunnerConstants.formatRoundProgress(currentRound, totalRounds)
                )
                ProgressBadge(
                    icon = Icons.Default.Timer,
                    label = TabataTimerRunnerConstants.LABEL_PHASE,
                    value = TabataTimerRunnerConstants.LABEL_ROUND_REST,
                    contentDescription = TabataTimerRunnerConstants.LABEL_ROUND_REST
                )
            }
            phase == TabataPhase.WARMUP || (phase == TabataPhase.PREPARE && warmupSeconds > 0) -> {
                Surface(
                    modifier = Modifier.semantics { contentDescription = TabataTimerRunnerConstants.SUBTITLE_WARMUP_PHASE },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = TabataTimerRunnerConstants.SUBTITLE_WARMUP_PHASE,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            phase == TabataPhase.COOLDOWN -> {
                Surface(
                    modifier = Modifier.semantics { contentDescription = TabataTimerRunnerConstants.SUBTITLE_COOLDOWN_PHASE },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = TabataTimerRunnerConstants.SUBTITLE_COOLDOWN_PHASE,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            phase == TabataPhase.COMPLETED -> {
                Surface(
                    modifier = Modifier.semantics { contentDescription = TabataTimerRunnerConstants.SUBTITLE_WORKOUT_FINISHED },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = TabataTimerRunnerConstants.SUBTITLE_WORKOUT_FINISHED,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBadge(
    icon: ImageVector,
    label: String,
    value: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
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
