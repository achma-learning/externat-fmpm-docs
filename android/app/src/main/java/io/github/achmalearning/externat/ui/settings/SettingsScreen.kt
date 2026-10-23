package io.github.achmalearning.externat.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.achmalearning.externat.BuildConfig
import io.github.achmalearning.externat.R
import io.github.achmalearning.externat.data.ThemeMode
import io.github.achmalearning.externat.data.UserState
import io.github.achmalearning.externat.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(user: UserState, viewModel: AppViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Section(stringResource(R.string.settings_appearance))
            listOf(
                ThemeMode.SYSTEM to R.string.theme_system,
                ThemeMode.LIGHT to R.string.theme_light,
                ThemeMode.DARK to R.string.theme_dark,
            ).forEach { (mode, label) ->
                ListItem(
                    headlineContent = { Text(stringResource(label)) },
                    leadingContent = { RadioButton(selected = user.themeMode == mode, onClick = null) },
                    modifier = Modifier.selectable(
                        selected = user.themeMode == mode,
                        role = Role.RadioButton,
                        onClick = { viewModel.setThemeMode(mode) },
                    ),
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchItem(
                    stringResource(R.string.dynamic_color),
                    stringResource(R.string.dynamic_color_summary),
                    user.dynamicColor,
                    viewModel::setDynamicColor,
                )
            }
            SwitchItem(
                stringResource(R.string.dark_pages),
                stringResource(R.string.dark_pages_summary),
                user.darkPages,
                viewModel::setDarkPages,
            )
            HorizontalDivider()
            Section(stringResource(R.string.settings_data))
            ListItem(
                headlineContent = { Text(stringResource(R.string.clear_history)) },
                supportingContent = { Text(stringResource(R.string.clear_history_message)) },
                leadingContent = { Icon(painterResource(R.drawable.ic_history), null) },
                modifier = Modifier.clickable(role = Role.Button, onClick = viewModel::clearRecents),
            )
            HorizontalDivider()
            Section(stringResource(R.string.settings_about))
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = { Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME)) },
                leadingContent = { Icon(painterResource(R.drawable.ic_info), null) },
            )
            listOf(R.string.about_text, R.string.about_privacy, R.string.about_sources).forEach {
                Text(
                    stringResource(it),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun SwitchItem(title: String, summary: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
    )
}
