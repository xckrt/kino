package com.example.kino.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kino.components.FormatButton
import com.example.kino.components.ReviewTypeButton
import com.example.kino.utils.parseHtml

@Composable
fun ReviewDialog(
    movieTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var type by remember { mutableStateOf("POSITIVE") }
    fun insertTag(tagName: String) {
        val currentText = textFieldValue.text
        val selection = textFieldValue.selection
        val tagStart = "<$tagName>"
        val tagEnd = "</$tagName>"
        val newText = StringBuilder(currentText)
            .insert(selection.max, tagEnd)
            .insert(selection.min, tagStart)
            .toString()
        val newCursorPos = if (selection.collapsed) {
            selection.min + tagStart.length
        } else {
            selection.max + tagStart.length + tagEnd.length
        }
        textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Отзыв на \"$movieTitle\"",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatButton(label = "B", isBold = true) { insertTag("b") }
                    FormatButton(label = "I", isBold = false) { insertTag("i") }
                }
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    placeholder = {
                        Text(
                            "Ваше мнение...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
                Spacer(Modifier.height(12.dp))
                if (textFieldValue.text.isNotEmpty()) {
                    Text(
                        text = "Предпросмотр:",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = textFieldValue.text.parseHtml(),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ReviewTypeButton("👍", type == "POSITIVE") { type = "POSITIVE" }
                    ReviewTypeButton("😐", type == "NEUTRAL") { type = "NEUTRAL" }
                    ReviewTypeButton("👎", type == "NEGATIVE") { type = "NEGATIVE" }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(textFieldValue.text, type) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Отправить", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}