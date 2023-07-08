// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dz.nexatech.reporter.client.common.AbstractLocalizer
import java.util.*

fun main() {
    application {
        val viewModel = remember { MainViewModel() }
        val defaultPadding = Configs.settings.paddingUnit
        Window(onCloseRequest = ::exitApplication) {
            MaterialTheme {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(defaultPadding * 2)
                ) {
                    Text(
                        text = viewModel.status.value,
                        modifier = Modifier.fillMaxWidth().padding(defaultPadding)
                    )
                    OutlinedTextField(
                        value = viewModel.workDir.value,
                        onValueChange = {
                            viewModel.updateWorkDir(it)
                        },
                        label = { Text("Work Directory") },
                        modifier = Modifier.fillMaxWidth().padding(defaultPadding)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = viewModel.htmlFileName.value,
                            onValueChange = {
                                viewModel.updateHtmlFileName(it)
                            },
                            label = { Text("HTML File") },
                            modifier = Modifier.padding(defaultPadding)
                        )

                        Button(
                            onClick = { viewModel.openWorkDirChooser(window) },
                            modifier = Modifier.padding(defaultPadding)
                        ) {
                            Text("Browse")
                        }

                        Button(
                            onClick = { viewModel.refreshAll() },
                            modifier = Modifier.padding(defaultPadding)
                        ) {
                            Text("Refresh")
                        }
                    }

                    Divider(Modifier.padding(defaultPadding * 2))

                    EpochMaker(defaultPadding)
                }
            }
        }
    }
}

@Composable
fun EpochMaker(defaultPadding: Dp) {

    var day: String by remember { mutableStateOf("") }
    var month: String by remember { mutableStateOf("") }
    var year: String by remember { mutableStateOf("") }

    val epochs: Epochs? by remember {
        derivedStateOf(structuralEqualityPolicy()) {
            dateToEpochs(year.toIntOrNull(), month.toIntOrNull(), day.toIntOrNull())
        }
    }

    Text(
        modifier = Modifier.padding(defaultPadding),
        text = "Date to Epoch Converter:",
    )

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(defaultPadding),
    ) {

        OutlinedTextField(
            value = day,
            onValueChange = { day = it },
            label = { Text("Day") },
            modifier = Modifier.padding(defaultPadding)
        )

        OutlinedTextField(
            value = month,
            onValueChange = { month = it },
            label = { Text("Month") },
            modifier = Modifier.padding(defaultPadding)
        )

        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") },
            modifier = Modifier.padding(defaultPadding)
        )
    }

    AnimatedVisibility(epochs != null) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(defaultPadding),
        ) {
            val min = epochs?.min ?: "N/A"
            val max = epochs?.max ?: "N/A"
            OutlinedTextField(
                value = min,
                readOnly = true,
                onValueChange = {},
                label = { Text("Date as MIN value") },
                modifier = Modifier.padding(defaultPadding)
            )
            OutlinedTextField(
                value = max,
                readOnly = true,
                onValueChange = {},
                label = { Text("Date as MAX value") },
                modifier = Modifier.padding(defaultPadding)
            )
        }
    }
}

fun dateToEpochs(year: Int?, month: Int?, day: Int?): Epochs? {
    if (year != null && month != null && day != null) {
        val calendar = AbstractLocalizer.newCalendar(year, month - 1, day)
        if (calendar.get(Calendar.YEAR) == year
            && calendar.get(Calendar.MONTH) == month - 1
            && calendar.get(Calendar.DAY_OF_MONTH) == day
        ) {
            val min = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val max = calendar.timeInMillis - 1

            return Epochs(min.toString(), max.toString())
        }
    }

    return null
}

@Immutable
data class Epochs(val min: String, val max: String)