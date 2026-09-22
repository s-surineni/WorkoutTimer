package com.example.workouttimer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.workouttimer.data.Exercise
import com.example.workouttimer.data.Workout
import com.example.workouttimer.theme.WorkoutTimerTheme

/** Constants and text formatting helpers for [TabataRoutineCard]. */
object TabataRoutineCardConstants {
    const val CD_SHARE_ROUTINE = "Share Routine"
    const val CD_EDIT_ROUTINE = "Edit Routine"
    const val CD_DELETE_ROUTINE = "Delete Routine"
    const val ACTION_SHOW_LESS = "Show Less"
    const val ACTION_VIEW_DETAILS = "View Details"
    const val ACTION_START = "Start"
    const val TITLE_SEQUENCE_DETAILS = "Workout Sequence Details:"

    fun summaryText(rounds: Int, exerciseCount: Int, formattedDuration: String): String =
        "$rounds Rounds • $exerciseCount Exercises • Total: $formattedDuration"

    fun warmupChipLabel(seconds: Int): String =
        "🔥 Warm-Up (${seconds}s)"

    fun warmupDetailLabel(seconds: Int): String =
        "• Warm-Up: ${seconds}s"

    fun exerciseChipLabel(name: String, workSeconds: Int, restSeconds: Int): String =
        "$name (${workSeconds}s/${restSeconds}s)"

    fun cooldownChipLabel(seconds: Int): String =
        "❄️ Cool-Down (${seconds}s)"

    fun cooldownDetailLabel(seconds: Int): String =
        "• Cool-Down: ${seconds}s"

    fun restBetweenRoundsDetail(seconds: Int): String =
        "• Rest Between Rounds: ${seconds}s"

    fun exerciseDetail(index: Int, name: String, workSeconds: Int, restSeconds: Int): String =
        "${index + 1}. $name: ${workSeconds}s Work / ${restSeconds}s Rest"
}

/**
 * Card displaying a Tabata workout routine summary, its warm-up/cool-down blocks, exercises, and Edit/Start/Delete actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabataRoutineCard(
    workout: Workout,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onShare: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Action buttons (Share, Edit & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = TabataRoutineCardConstants.summaryText(
                                rounds = workout.rounds,
                                exerciseCount = workout.exercises.size,
                                formattedDuration = workout.formattedTotalDuration()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = TabataRoutineCardConstants.CD_SHARE_ROUTINE,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = TabataRoutineCardConstants.CD_EDIT_ROUTINE,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = TabataRoutineCardConstants.CD_DELETE_ROUTINE,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Warmup, Exercises & Cooldown chips preview
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (workout.warmupSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = TabataRoutineCardConstants.warmupChipLabel(workout.warmupSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                workout.exercises.forEach { exercise ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = TabataRoutineCardConstants.exerciseChipLabel(exercise.name, exercise.workSeconds, exercise.restSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (workout.cooldownSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = TabataRoutineCardConstants.cooldownChipLabel(workout.cooldownSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Expandable details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = TabataRoutineCardConstants.TITLE_SEQUENCE_DETAILS,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (workout.warmupSeconds > 0) {
                        Text(
                            text = TabataRoutineCardConstants.warmupDetailLabel(workout.warmupSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    workout.exercises.forEachIndexed { index, exercise ->
                        Text(
                            text = TabataRoutineCardConstants.exerciseDetail(index, exercise.name, exercise.workSeconds, exercise.restSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (workout.rounds > 1) {
                        Text(
                            text = TabataRoutineCardConstants.restBetweenRoundsDetail(workout.restBetweenRoundsSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (workout.cooldownSeconds > 0) {
                        Text(
                            text = TabataRoutineCardConstants.cooldownDetailLabel(workout.cooldownSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Start Tabata and Expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (expanded) TabataRoutineCardConstants.ACTION_SHOW_LESS else TabataRoutineCardConstants.ACTION_VIEW_DETAILS)
                }

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(TabataRoutineCardConstants.ACTION_START)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TabataRoutineCardPreview() {
    WorkoutTimerTheme {
        TabataRoutineCard(
            workout = Workout(
                title = "Classic Tabata",
                rounds = 2,
                restBetweenRoundsSeconds = 30,
                warmupSeconds = 30,
                cooldownSeconds = 30,
                exercises = listOf(
                    Exercise(name = "Jumping Jacks", workSeconds = 20, restSeconds = 10),
                    Exercise(name = "Push Ups", workSeconds = 20, restSeconds = 10),
                    Exercise(name = "Bodyweight Squats", workSeconds = 20, restSeconds = 10),
                )
            ),
            onStart = {},
            onEdit = {},
            onDelete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
