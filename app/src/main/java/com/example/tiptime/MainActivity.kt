/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFBBDEFB)
                ) {
                    TipTimeLayout()
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout() {
    var amountInput by remember { mutableStateOf("") }
    var peopleCount by remember { mutableStateOf("") }
    var roundUp by remember { mutableStateOf(false) }
    var selectedTipPercent by remember { mutableStateOf(15.0) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = selectedTipPercent
    val people = peopleCount.toIntOrNull() ?: 1
    val tip = calculateTip(amount, tipPercent, roundUp)
    val total = amount + tip
    val totalPerPerson = total / people

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start),
            color = Color(0xFF0D47A1)
        )
        EditNumberField(
            label = R.string.bill_amount,
            leadingIcon = R.drawable.money,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = amountInput,
            onValueChanged = { amountInput = it },
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
        )
        TipPercentageRadioGroup(
            selectedTipPercent = selectedTipPercent,
            onTipPercentSelected = { selectedTipPercent = it },
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
        )
        EditNumberField(
            label = R.string.people_count,
            leadingIcon = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            value = peopleCount,
            onValueChanged = { peopleCount = it },
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
        )
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFF0D47A1)
        )
        Text(
            text = stringResource(R.string.total_amount, total),
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFF0D47A1)
        )
        Text(
            text = stringResource(R.string.total_per_person, totalPerPerson),
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFF0D47A1)
        )
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun EditNumberField(
    @StringRes label: Int,
    @DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        singleLine = true,
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), contentDescription = null) },
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun TipPercentageRadioGroup(
    selectedTipPercent: Double,
    onTipPercentSelected: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val tipPercentages = listOf(10.0, 15.0, 20.0, 25.0)

    Column(modifier = modifier) {
        Text("Tip Percentage", style = MaterialTheme.typography.bodyLarge)
        tipPercentages.forEach { percent ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTipPercentSelected(percent) }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = percent == selectedTipPercent,
                    onClick = { onTipPercentSelected(percent) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$percent%")
            }
        }
    }
}

@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.round_up_tip))
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */
private fun calculateTip(amount: Double, tipPercent: Double, roundUp: Boolean): Double {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }
    return tip
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFBBDEFB)
        ) {
            TipTimeLayout()
        }
    }
}
