package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import com.example.ui.theme.AstraRoseLight
import com.example.viewmodel.AstraViewModel

data class HubTool(
    val title: String,
    val description: String,
    val prompt: String,
    val icon: ImageVector,
    val category: String,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    viewModel: AstraViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Daily Life", "Tech & Code", "Academic", "Brainstorming")
    var selectedCategory by remember { mutableStateOf("All") }

    val tools = remember {
        listOf(
            HubTool(
                title = "30-Min Energizing Routine",
                description = "Custom morning or evening routine designed for maximum clarity.",
                prompt = "Please build a realistic, 30-minute high-energy morning routine with exact minutes and practical habits.",
                icon = Icons.Default.WbSunny,
                category = "Daily Life",
                accentColor = Color(0xFFF59E0B)
            ),
            HubTool(
                title = "Ghar Ka Smart Budget (50/30/20)",
                description = "Manage household expenses and savings in simple Hindi/English terms.",
                prompt = "Meri monthly income ko 50/30/20 rule ke hisab se organize karne ka practical step-by-step plan banayein, jisme emergency fund aur savings include ho.",
                icon = Icons.Default.Savings,
                category = "Daily Life",
                accentColor = Color(0xFF10B981)
            ),
            HubTool(
                title = "Meal & Nutrition Planner",
                description = "Fast, nutritious meals with minimal kitchen cleanup.",
                prompt = "Suggest a 3-day balanced meal plan using common ingredients, quick prep time (under 25 mins), and high protein.",
                icon = Icons.Default.Restaurant,
                category = "Daily Life",
                accentColor = Color(0xFFEC4899)
            ),
            HubTool(
                title = "Code Debugger & Explainer",
                description = "Paste any error or snippet (Python, Kotlin, JS) for line-by-line fix.",
                prompt = "I have a coding issue. Please explain the bug step-by-step and provide the optimized, clean solution with comments.",
                icon = Icons.Default.Code,
                category = "Tech & Code",
                accentColor = AstraIndigoLight
            ),
            HubTool(
                title = "System Architecture Blueprint",
                description = "Plan databases, API flows, and mobile/backend structures cleanly.",
                prompt = "Help me design the high-level architecture and database schema for a modern scalable mobile app.",
                icon = Icons.Default.Storage,
                category = "Tech & Code",
                accentColor = AstraCyanLight
            ),
            HubTool(
                title = "Complex Science Made Simple",
                description = "Physics, Quantum, Math, or Biology explained with analogies.",
                prompt = "Explain how Quantum Computing and superposition work using a simple everyday real-world analogy.",
                icon = Icons.Default.Science,
                category = "Academic",
                accentColor = Color(0xFF8B5CF6)
            ),
            HubTool(
                title = "Research Paper & Essay Outline",
                description = "Structure persuasive arguments, thesis, and evidence logically.",
                prompt = "Create a structured, compelling academic outline with an introduction, 3 core arguments, counter-argument, and conclusion.",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                category = "Academic",
                accentColor = Color(0xFF3B82F6)
            ),
            HubTool(
                title = "5 High-Impact Viral Hooks",
                description = "Psychological hook formulas for products, reels, and newsletters.",
                prompt = "Brainstorm 5 high-converting viral hooks and headlines for a tech productivity product targeting young professionals.",
                icon = Icons.Default.Lightbulb,
                category = "Brainstorming",
                accentColor = AstraRoseLight
            ),
            HubTool(
                title = "Startup Value Proposition",
                description = "Sharpen your business elevator pitch and customer differentiators.",
                prompt = "Help me crystallize a crisp, punchy elevator pitch and 3 competitive differentiators for a new startup idea.",
                icon = Icons.Default.RocketLaunch,
                category = "Brainstorming",
                accentColor = Color(0xFFE11D48)
            )
        )
    }

    val filteredTools = remember(selectedCategory) {
        if (selectedCategory == "All") tools else tools.filter { it.category == selectedCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Astra Toolkits & Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AstraIndigoLight,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Tools List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Header card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(AstraIndigoLight, AstraCyanLight))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Ready-To-Solve Blueprints",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tap any toolkit to launch a focused session with Astra.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                items(filteredTools.size) { index ->
                    val tool = filteredTools[index]
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.createNewConversation(
                                    initialPrompt = tool.prompt,
                                    category = tool.category
                                )
                                onNavigateToChat()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tool.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = tool.title,
                                    tint = tool.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tool.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tool.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Launch",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
