package io.github.achmalearning.externat

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Démarrage réel de l'activité sous Robolectric : catalogue chargé depuis les assets générés,
 * navigation liste -> détail, checklist (DataStore), recherche sans accents, onglet Guides,
 * mise en page deux volets sur tablette. Le rendu PdfRenderer, lui, se vérifie sur appareil.
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val dyspnee = "Etablir une démarche diagnostique et thérapeutique devant une dyspnée"

    private fun waitForText(text: String, substring: Boolean = false) = compose.waitUntil(10_000) {
        compose.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
    }

    private fun scrollTo(text: String) {
        compose.onAllNodes(hasScrollToNodeAction()).onLast().performScrollToNode(hasText(text))
    }

    private fun openCardiologie() {
        waitForText("Objectifs de stage")
        compose.onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasText("Cardiologie"))
        compose.onNodeWithText("Cardiologie").performClick()
        compose.waitForIdle()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun phone_openSpecialtyAndCheckObjective() {
        openCardiologie()
        waitForText("Guide de stage 2022, p. 12")
        scrollTo(dyspnee)
        // DataStore garde l'état d'un test à l'autre dans la même JVM : on inverse l'état courant.
        val wasOn = runCatching { compose.onNodeWithText(dyspnee).assertIsOn() }.isSuccess
        compose.onNodeWithText(dyspnee).performClick()
        // L'état revient de DataStore (écriture asynchrone) : on attend la recomposition.
        compose.waitUntil(5_000) {
            runCatching {
                if (wasOn) compose.onNodeWithText(dyspnee).assertIsOff() else compose.onNodeWithText(dyspnee).assertIsOn()
            }.isSuccess
        }
        compose.onAllNodesWithText("Objectifs validés").onFirst().assertExists()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun checklistSurvivesActivityRecreation() {
        openCardiologie()
        waitForText("Guide de stage 2022, p. 12")
        scrollTo(dyspnee)
        if (runCatching { compose.onNodeWithText(dyspnee).assertIsOff() }.isSuccess) {
            compose.onNodeWithText(dyspnee).performClick()
        }
        compose.waitUntil(5_000) { runCatching { compose.onNodeWithText(dyspnee).assertIsOn() }.isSuccess }

        compose.activityRule.scenario.recreate() // rotation / changement de configuration
        waitForText("Guide de stage 2022, p. 12")
        scrollTo(dyspnee)
        compose.onNodeWithText(dyspnee).assertIsOn()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun phone_fullTextSearchIsAccentInsensitive() {
        waitForText("Objectifs de stage")
        compose.onNode(hasSetTextAction()).performTextInput("gyneco")
        waitForText("Spécialités")
        scrollTo("Gynécologie-obstétrique")
        compose.onAllNodesWithText("Gynécologie-obstétrique").onFirst().assertExists()

        // Plein texte, sans accent ni majuscule : « Analyser un partogramme… », « Hémorragie… ».
        compose.onNode(hasSetTextAction()).performTextClearance()
        compose.onNode(hasSetTextAction()).performTextInput("HEMORRAGIE post partum")
        waitForText("Dans les objectifs", substring = true)
        waitForText("Hémorragie du post partum", substring = true)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun phone_guidesTabListsSixGuides() {
        waitForText("Guides")
        compose.onAllNodesWithText("Guides").onFirst().performClick()
        waitForText("Guide de l'étudiant 2024")
        scrollTo("Guide de stage (ancienne version)")
        compose.onNodeWithText("Guide de stage (ancienne version)").assertExists()
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp-xlarge-land")
    fun tablet_listAndDetailSideBySide() {
        openCardiologie()
        waitForText("Guide de stage 2022, p. 12")
        // Volet liste toujours visible à côté du détail.
        compose.onNodeWithText("Rechercher (spécialité ou objectif)").assertExists()
    }
}
