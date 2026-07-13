package com.example.insightself.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.insightself.data.local.RegionCatalog
import com.example.insightself.data.local.RegionNameTranslator
import com.example.insightself.data.local.RegionProvince

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthRegionPicker(
    provinces: List<RegionProvince>,
    selectedProvince: String,
    selectedCity: String,
    selectedDistrict: String,
    onSelectionChange: (province: String, city: String, district: String) -> Unit,
    isChinese: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    remember(isChinese) { RegionNameTranslator.load(context) }

    fun label(zhName: String) = RegionNameTranslator.displayName(context, zhName, isChinese)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RequiredFieldLabel(
            text = if (isChinese) "出生地点" else "Birthplace",
            requirement = ProfileFieldRequirements.birthplace,
            isChinese = isChinese
        )
        RegionDropdown(
            label = if (isChinese) "省 / 直辖市 / 特别行政区" else "Province / Municipality",
            value = label(selectedProvince),
            options = provinces.map { it.name to label(it.name) },
            onSelected = { provinceName ->
                val province = provinces.firstOrNull { it.name == provinceName } ?: return@RegionDropdown
                val city = province.cities.firstOrNull()?.name.orEmpty()
                val district = province.cities.firstOrNull()?.districts?.firstOrNull()?.name.orEmpty()
                onSelectionChange(provinceName, city, district)
            }
        )
        val cities = provinces.firstOrNull { it.name == selectedProvince }?.cities.orEmpty()
        RegionDropdown(
            label = if (isChinese) "城市" else "City",
            value = label(selectedCity),
            options = cities.map { it.name to label(it.name) },
            enabled = cities.isNotEmpty(),
            onSelected = { cityName ->
                val city = cities.firstOrNull { it.name == cityName } ?: return@RegionDropdown
                val district = city.districts.firstOrNull()?.name.orEmpty()
                onSelectionChange(selectedProvince, cityName, district)
            }
        )
        val districts = cities.firstOrNull { it.name == selectedCity }?.districts.orEmpty()
        RegionDropdown(
            label = if (isChinese) "区 / 县" else "District",
            value = label(selectedDistrict),
            options = districts.map { it.name to label(it.name) },
            enabled = districts.isNotEmpty(),
            onSelected = { districtName ->
                onSelectionChange(selectedProvince, selectedCity, districtName)
            }
        )
        val formatted = RegionCatalog.formatBirthPlace(selectedProvince, selectedCity, selectedDistrict)
        if (formatted.isNotBlank() && !formatted.contains("||")) {
            val displayFormatted = listOf(selectedProvince, selectedCity, selectedDistrict)
                .joinToString(" / ") { label(it) }
            Text(
                text = if (isChinese) "已选：$displayFormatted" else "Selected: $displayFormatted",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true)
        ) {
            options.forEach { (canonical, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelected(canonical)
                        expanded = false
                    }
                )
            }
        }
    }
}
