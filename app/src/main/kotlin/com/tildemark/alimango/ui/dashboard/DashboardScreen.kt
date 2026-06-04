package com.tildemark.alimango.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tildemark.alimango.domain.usecase.SyncStatus
import com.tildemark.alimango.ui.theme.WaniKaniBlue
import com.tildemark.alimango.ui.theme.WaniKaniPink
import com.tildemark.alimango.ui.theme.WaniKaniPurple
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.tildemark.alimango.domain.usecase.LevelProgress
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.ui.browser.SubjectDetailDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartReviews: () -> Unit,
    onStartLessons: (List<Int>?) -> Unit,
    onBrowseItems: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val syncState by viewModel.syncStatus.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val progress by viewModel.levelProgress.collectAsState()
    
    val activeDetailSubject by viewModel.selectedSubject.collectAsState()
    val activeAssignment by viewModel.selectedAssignment.collectAsState()
    val activeRelationships by viewModel.selectedRelationships.collectAsState()

    var showLessonPicker by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            SyncStatusBar(syncState = syncState, onSyncClick = { viewModel.triggerSync() })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Konnichiwa,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = user?.username ?: "WaniKani Learner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBrowseItems) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Subjects",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Level Badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(WaniKaniPink, WaniKaniPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${user?.level ?: 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reviews / Lessons Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val reviews = summary?.reviewCount ?: 0
                // Active Reviews Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .clickable(enabled = reviews > 0) { onStartReviews() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (reviews > 0) WaniKaniPink else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reviews",
                            fontWeight = FontWeight.Bold,
                            color = if (reviews > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "$reviews",
                            fontWeight = FontWeight.Black,
                            color = if (reviews > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 44.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Lessons Card
                val lessons = summary?.lessonCount ?: 0
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lessons > 0) WaniKaniPurple else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (lessons > 0) 4.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lessons",
                                fontWeight = FontWeight.Bold,
                                color = if (lessons > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "$lessons",
                                fontWeight = FontWeight.Black,
                                color = if (lessons > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 32.sp
                            )
                        }

                        if (lessons > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Start Lessons Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { onStartLessons(null) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Start",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                // Advanced button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { showLessonPicker = true }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Advanced",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No lessons",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Forecast block
            SrsForecastCard(forecast = summary?.hourlyForecast ?: emptyList())

            Spacer(modifier = Modifier.height(10.dp))

            // Level Progress Card
            progress?.let { prog ->
                LevelProgressCard(progress = prog, onSubjectClick = { viewModel.showSubjectDetail(it) })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SRS breakdown summary
            Text(
                text = "SRS Progress Stages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SrsStageItem(name = "Apprentice (1 - 4)", count = summary?.apprenticeCount ?: 0, color = WaniKaniPink)
                    SrsStageItem(name = "Guru (5 - 6)", count = summary?.guruCount ?: 0, color = WaniKaniPurple)
                    SrsStageItem(name = "Master (7)", count = summary?.masterCount ?: 0, color = WaniKaniBlue)
                    SrsStageItem(name = "Enlightened (8)", count = summary?.enlightenedCount ?: 0, color = Color(0xFFFF9800))
                    SrsStageItem(name = "Burned (9)", count = summary?.burnedCount ?: 0, color = Color(0xFF4CAF50))
                }
            }
        }
    }

    // Detail dialog when an item is clicked
    activeDetailSubject?.let { subject ->
        SubjectDetailDialog(
            subject = subject,
            assignment = activeAssignment,
            relationships = activeRelationships,
            onDismiss = { viewModel.showSubjectDetail(null) },
            onSaveNotesAndSynonyms = { note, synonyms ->
                viewModel.saveNoteAndSynonyms(subject.id, note, synonyms)
            },
            onNavigateToSubject = { targetSubj ->
                viewModel.showSubjectDetail(targetSubj)
            }
        )
    }

    if (showLessonPicker) {
        val availableLessons by viewModel.availableLessonSubjects.collectAsState()
        LessonPickerDialog(
            availableSubjects = availableLessons,
            onDismiss = { showLessonPicker = false },
            onStartCustomLessons = { selectedIds ->
                showLessonPicker = false
                onStartLessons(selectedIds)
            }
        )
    }
}

@Composable
fun SrsStageItem(name: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SyncStatusBar(
    syncState: SyncStatus,
    onSyncClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (syncState is SyncStatus.SyncingSubjects || syncState is SyncStatus.SyncingAssignments) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = WaniKaniPink
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                val text = when (syncState) {
                    is SyncStatus.Idle -> "Offline sync ready"
                    is SyncStatus.SyncingSubjects -> "Syncing database subjects..."
                    is SyncStatus.SyncingAssignments -> "Syncing user assignments..."
                    is SyncStatus.Success -> "Database is up to date"
                    is SyncStatus.Failed -> "Sync failed. Retry?"
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onSyncClick,
                enabled = syncState !is SyncStatus.SyncingSubjects && syncState !is SyncStatus.SyncingAssignments
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Now",
                    tint = WaniKaniPink
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelProgressCard(
    progress: LevelProgress,
    onSubjectClick: (Subject) -> Unit
) {
    val kanjiProgress = if (progress.kanjiTotal > 0) {
        progress.kanjiPassed.toFloat() / progress.kanjiTotal.toFloat()
    } else {
        0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Level ${progress.currentLevel} Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Guru at least 90% of current level Kanji to level up",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Kanji Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kanji Guru Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${progress.kanjiPassed} / ${progress.kanjiTotal} (${(kanjiProgress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaniKaniPink
                    )
                }
                LinearProgressIndicator(
                    progress = { kanjiProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = WaniKaniPink,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Radicals section
            val radicals = progress.items.filter { it.subject.type == "radical" }
            if (radicals.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Radicals Progress (${progress.radicalsPassed}/${progress.radicalsTotal})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaniKaniBlue
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        radicals.forEach { item ->
                            LevelSubjectChip(
                                character = item.subject.characters ?: "",
                                srsStage = item.srsStage,
                                isPassed = item.isPassed,
                                baseColor = WaniKaniBlue,
                                onClick = { onSubjectClick(item.subject) }
                            )
                        }
                    }
                }
            }

            // Kanji section
            val kanji = progress.items.filter { it.subject.type == "kanji" }
            if (kanji.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kanji Progress (${progress.kanjiPassed}/${progress.kanjiTotal})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaniKaniPink
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        kanji.forEach { item ->
                            LevelSubjectChip(
                                character = item.subject.characters ?: "",
                                srsStage = item.srsStage,
                                isPassed = item.isPassed,
                                baseColor = WaniKaniPink,
                                onClick = { onSubjectClick(item.subject) }
                            )
                        }
                    }
                }
            }

            // Vocabulary section
            val vocab = progress.items.filter { it.subject.type == "vocabulary" || it.subject.type == "kana_vocabulary" }
            if (vocab.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Vocabulary Progress (${progress.vocabPassed}/${progress.vocabTotal})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaniKaniPurple
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        vocab.forEach { item ->
                            LevelSubjectChip(
                                character = item.subject.characters ?: "",
                                srsStage = item.srsStage,
                                isPassed = item.isPassed,
                                baseColor = WaniKaniPurple,
                                onClick = { onSubjectClick(item.subject) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelSubjectChip(
    character: String,
    srsStage: Int?,
    isPassed: Boolean,
    baseColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isPassed -> baseColor
        srsStage != null -> baseColor.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        srsStage != null -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val borderModifier = if (srsStage != null && !isPassed) {
        Modifier.background(backgroundColor, RoundedCornerShape(4.dp))
    } else {
        Modifier.background(backgroundColor, RoundedCornerShape(4.dp))
    }

    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 32.dp)
            .then(borderModifier)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = character,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun SrsForecastCard(forecast: List<Int>) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    val totalReviews = forecast.sum()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "24-Hour Review Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val detailText = if (selectedHour != null) {
                        "Hour +${selectedHour!! + 1}: ${forecast[selectedHour!!]} reviews"
                    } else {
                        "Tap a bar to inspect hours"
                    }
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WaniKaniPink.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+$totalReviews",
                        color = WaniKaniPink,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bars row
            if (forecast.isNotEmpty()) {
                val maxReviews = (forecast.maxOrNull() ?: 1).coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    forecast.forEachIndexed { index, count ->
                        val barHeightFactor = count.toFloat() / maxReviews.toFloat()
                        val barHeight = (barHeightFactor * 80).coerceAtLeast(4f).dp
                        val isSelected = selectedHour == index

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(barHeight)
                                .padding(horizontal = 1.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isSelected) WaniKaniPink else WaniKaniPink.copy(alpha = 0.4f)
                                )
                                .clickable { selectedHour = if (isSelected) null else index }
                        )
                    }
                }

                // Axis Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "+1h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text(text = "+6h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text(text = "+12h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text(text = "+18h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text(text = "+24h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            } else {
                Text(
                    text = "No forecast data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonPickerDialog(
    availableSubjects: List<Subject>,
    onDismiss: () -> Unit,
    onStartCustomLessons: (List<Int>) -> Unit
) {
    var selectedIds by remember(availableSubjects) {
        mutableStateOf(availableSubjects.map { it.id }.toSet())
    }
    var selectedTypeFilter by remember { mutableStateOf("all") } // "all", "radical", "kanji", "vocabulary"

    val filteredSubjects = remember(availableSubjects, selectedTypeFilter) {
        if (selectedTypeFilter == "all") {
            availableSubjects
        } else {
            availableSubjects.filter { it.type == selectedTypeFilter || (selectedTypeFilter == "vocabulary" && it.type == "kana_vocabulary") }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(600.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Lesson Picker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Choose the items you want to learn in this custom lesson session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Category Tabs / Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "all" to "All",
                        "radical" to "Radicals",
                        "kanji" to "Kanji",
                        "vocabulary" to "Vocabulary"
                    )
                    filters.forEach { (type, label) ->
                        val isSelected = selectedTypeFilter == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) {
                                        when (type) {
                                            "radical" -> WaniKaniBlue
                                            "kanji" -> WaniKaniPink
                                            "vocabulary" -> WaniKaniPurple
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { selectedTypeFilter = type }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bulk Selection Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select all",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                val currentFilteredIds = filteredSubjects.map { it.id }.toSet()
                                selectedIds = selectedIds + currentFilteredIds
                            }
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Clear selection",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                val currentFilteredIds = filteredSubjects.map { it.id }.toSet()
                                selectedIds = selectedIds - currentFilteredIds
                            }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Subject Grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    if (filteredSubjects.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredSubjects.forEach { subject ->
                                    val isChecked = selectedIds.contains(subject.id)
                                    val color = when (subject.type) {
                                        "radical" -> WaniKaniBlue
                                        "kanji" -> WaniKaniPink
                                        else -> WaniKaniPurple
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isChecked) color else color.copy(alpha = 0.15f)
                                            )
                                            .clickable {
                                                selectedIds = if (isChecked) {
                                                    selectedIds - subject.id
                                                } else {
                                                    selectedIds + subject.id
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = subject.characters ?: subject.meanings.firstOrNull() ?: "?",
                                                color = if (isChecked) Color.White else color,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            if (isChecked) {
                                                Text(
                                                    text = "✓",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val hasSelection = selectedIds.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            .clickable(enabled = hasSelection) {
                                onStartCustomLessons(selectedIds.toList())
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start (${selectedIds.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (hasSelection) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

