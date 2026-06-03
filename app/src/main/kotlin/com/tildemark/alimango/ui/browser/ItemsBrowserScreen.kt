package com.tildemark.alimango.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.ui.theme.WaniKaniBlue
import com.tildemark.alimango.ui.theme.WaniKaniPink
import com.tildemark.alimango.ui.theme.WaniKaniPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsBrowserScreen(
    viewModel: ItemsBrowserViewModel,
    onBackClick: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedMenu by viewModel.selectedMenu.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val availableLevels by viewModel.availableLevels.collectAsState()

    val activeDetailSubject by viewModel.selectedSubject.collectAsState()
    val activeAssignment by viewModel.selectedAssignment.collectAsState()
    val activeRelationships by viewModel.selectedRelationships.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Items Browser") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search by characters, meanings...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WaniKaniPink,
                    focusedLabelColor = WaniKaniPink,
                    cursorColor = WaniKaniPink
                )
            )

            // Subject Menu Selector (WaniKani Menu Categories)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilterChip("Levels", selectedMenu == "levels", WaniKaniPurple) {
                    viewModel.onMenuSelected("levels")
                }
                TypeFilterChip("Radicals", selectedMenu == "radical", WaniKaniBlue) {
                    viewModel.onMenuSelected("radical")
                }
                TypeFilterChip("Kanji", selectedMenu == "kanji", WaniKaniPink) {
                    viewModel.onMenuSelected("kanji")
                }
                TypeFilterChip("Vocabulary", selectedMenu == "vocabulary", WaniKaniPurple) {
                    viewModel.onMenuSelected("vocabulary")
                }
            }

            // Level Selector Submenu (only visible when "Levels" menu is active)
            if (selectedMenu == "levels" && availableLevels.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(availableLevels) { lvl ->
                        LevelFilterChip("Level $lvl", selectedLevel == lvl) {
                            viewModel.onLevelSelected(lvl)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subjects found.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subjects) { subject ->
                        SubjectBrowserItem(
                            subject = subject,
                            onClick = { viewModel.showSubjectDetail(subject) }
                        )
                    }
                }
            }
        }
    }

    // Detail dialog when a card is clicked
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
}

@Composable
fun SubjectDetailDialog(
    subject: Subject,
    assignment: Assignment?,
    relationships: List<Subject>,
    onDismiss: () -> Unit,
    onSaveNotesAndSynonyms: (String, List<String>) -> Unit,
    onNavigateToSubject: (Subject) -> Unit
) {
    var note by remember(subject.id, assignment) { mutableStateOf(assignment?.note ?: "") }
    var synonymsText by remember(subject.id, assignment) {
        mutableStateOf(assignment?.userSynonyms?.joinToString(", ") ?: "")
    }

    val typeColor = when (subject.type) {
        "radical" -> WaniKaniBlue
        "kanji" -> WaniKaniPink
        "vocabulary", "kana_vocabulary" -> WaniKaniPurple
        else -> WaniKaniPink
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (Type / Level / Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(typeColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = subject.type.replaceFirstChar { it.uppercase() },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Level ${subject.level}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = WaniKaniPink)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detail Content (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Big Character
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(typeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject.characters ?: (subject.meanings.firstOrNull() ?: ""),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Meanings / Primary
                    Column {
                        Text(
                            text = "Meanings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaniKaniPink
                        )
                        Text(
                            text = subject.meanings.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Readings (If Kanji or Vocab)
                    if (subject.readings.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Readings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaniKaniPink
                            )
                            Text(
                                text = subject.readings.joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Progression / SRS stage
                    Column {
                        Text(
                            text = "My Progression",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaniKaniPink
                        )
                        val srsStageStr = assignment?.srsStageName ?: "Locked (Not started)"
                        val stageColor = when (assignment?.srsStage) {
                            in 1..4 -> WaniKaniPink
                            in 5..6 -> WaniKaniPurple
                            7 -> WaniKaniBlue
                            8 -> Color(0xFFFF9800)
                            9 -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(stageColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = srsStageStr,
                                color = stageColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Meaning Mnemonic
                    Column {
                        Text(
                            text = "Meaning Mnemonic",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaniKaniPink
                        )
                        Text(
                            text = stripHtmlTags(subject.meaningMnemonic),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }

                    // Reading Mnemonic
                    if (!subject.readingMnemonic.isNullOrBlank()) {
                        Column {
                            Text(
                                text = "Reading Mnemonic",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaniKaniPink
                            )
                            Text(
                                text = stripHtmlTags(subject.readingMnemonic),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // User Synonyms
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "User Synonyms",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaniKaniPink
                        )
                        OutlinedTextField(
                            value = synonymsText,
                            onValueChange = { synonymsText = it },
                            placeholder = { Text("Add synonyms, separated by commas") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaniKaniPink,
                                cursorColor = WaniKaniPink
                            )
                        )
                    }

                    // User Study Note
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Study Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WaniKaniPink
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("Write personal mnemonics, notes...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WaniKaniPink,
                                cursorColor = WaniKaniPink
                            )
                        )
                    }

                    // Relationships (Found in Kanji / Composition)
                    if (relationships.isNotEmpty()) {
                        val relationTitle = when (subject.type) {
                            "radical" -> "Found in Kanji"
                            "kanji" -> "Found in Vocabulary"
                            else -> "Kanji Composition"
                        }
                        Column {
                            Text(
                                text = relationTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaniKaniPink
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(relationships) { item ->
                                    val relColor = when (item.type) {
                                        "radical" -> WaniKaniBlue
                                        "kanji" -> WaniKaniPink
                                        else -> WaniKaniPurple
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(relColor)
                                            .clickable { onNavigateToSubject(item) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.characters ?: "",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer (Save changes button)
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val synonyms = synonymsText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        onSaveNotesAndSynonyms(note, synonyms)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaniKaniPink)
                ) {
                    Text("Save Study Info", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun stripHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
}

@Composable
fun SubjectBrowserItem(
    subject: Subject,
    onClick: () -> Unit
) {
    val cardColor = when (subject.type) {
        "radical" -> WaniKaniBlue
        "kanji" -> WaniKaniPink
        "vocabulary", "kana_vocabulary" -> WaniKaniPurple
        else -> WaniKaniPink
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level label
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${subject.level}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Type label
                val typeName = when (subject.type) {
                    "radical" -> "Radical"
                    "kanji" -> "Kanji"
                    "vocabulary", "kana_vocabulary" -> "Vocabulary"
                    else -> "Subject"
                }
                Text(
                    text = typeName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject Character
            Text(
                text = subject.characters ?: (subject.meanings.firstOrNull() ?: ""),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Meanings list (first meaning)
            Text(
                text = subject.meanings.firstOrNull() ?: "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TypeFilterChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LevelFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) WaniKaniPink else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

