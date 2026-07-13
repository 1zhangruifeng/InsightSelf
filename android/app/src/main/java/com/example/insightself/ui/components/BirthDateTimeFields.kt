package com.example.insightself.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDateTimeFields(
    birthDate: String,
    birthTime: String,
    onBirthDateChange: (String) -> Unit,
    onBirthTimeChange: (String) -> Unit,
    isChinese: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeParseFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }

    val initialDateMillis = remember(birthDate) {
        runCatching { LocalDate.parse(birthDate, dateFormatter) }
            .getOrNull()
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    }

    val initialTime = remember(birthTime) {
        val normalized = birthTimeHourMinute(birthTime).ifBlank { birthTime }
        runCatching { LocalTime.parse(normalized, timeFormatter) }
            .recoverCatching { LocalTime.parse(birthTime, timeParseFormatter) }
            .getOrNull() ?: LocalTime.NOON
    }

    val dateLabel = if (isChinese) "出生日期" else "Birth date"
    val timeLabel = if (isChinese) "出生时间" else "Birth time"

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    RequiredFieldLabel(
                        text = dateLabel,
                        requirement = ProfileFieldRequirements.birthDate,
                        isChinese = isChinese,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = birthDate.ifBlank { if (isChinese) "点击选择" else "Tap to select" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    RequiredFieldLabel(
                        text = timeLabel,
                        requirement = ProfileFieldRequirements.birthTime,
                        isChinese = isChinese,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = birthTimeHourMinute(birthTime).ifBlank { birthTime }
                            .ifBlank { if (isChinese) "点击选择" else "Tap to select" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onBirthDateChange(date.format(dateFormatter))
                        }
                        showDatePicker = false
                    }
                ) { Text(if (isChinese) "确认" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isChinese) "取消" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute, 0)
                        onBirthTimeChange(time.format(DateTimeFormatter.ofPattern("HH:mm")))
                        showTimePicker = false
                    }
                ) { Text(if (isChinese) "确认" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(if (isChinese) "取消" else "Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
