// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    val defaultPadding = 4.dp
    application {
        val viewModel = remember { MainViewModel() }
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
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.padding(defaultPadding)) {
                            Text("Refresh")
                        }
                    }
                }
            }
        }
    }
}