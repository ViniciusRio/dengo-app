package com.viniciusrio.dengo.ui.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.navigation.PrototypePerspective
import com.viniciusrio.dengo.ui.theme.DengoTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun lidianneReadsAllRecordedSteps() = assertRecordedSteps(PrototypePerspective.LIDIANNE)

    @Test fun viniciusReadsAllRecordedSteps() = assertRecordedSteps(PrototypePerspective.VINICIUS)

    @Test fun declinedOtherRequestKeepsOriginalLongText() {
        val date = LocalDate.of(2026, 9, 30)
        val message = "Pode trazer uma bolsa quente e deixar uma garrafa de água perto de mim quando chegar?"
        val createdAt = Instant.parse("2026-09-30T14:00:00Z")
        val declinedAt = Instant.parse("2026-09-30T14:01:00Z")
        val story = RequestStory(
            requestId = 2,
            requestType = CareRequestType.OTHER,
            requestMessage = message,
            steps = listOf(
                RequestStoryStep(HistoryEvent.RequestCreated(2, PartnerId.LIDIANNE, createdAt), date, LocalTime.of(14, 0)),
                RequestStoryStep(HistoryEvent.RequestDeclined(2, PartnerId.VINICIUS, declinedAt), date, LocalTime.of(14, 1)),
            ),
            occurredAt = declinedAt,
            originalIndex = 1,
            date = date,
        )
        composeRule.setContent {
            DengoTheme {
                HistoryScreen(HistoryUiState(listOf(HistoryDay(date, listOf(story)))), PrototypePerspective.VINICIUS, today = date)
            }
        }

        composeRule.onNodeWithContentDescription("Lidianne fez um pedido. $message. 30/09/2026, 14:00").assertExists()
        composeRule.onNodeWithContentDescription("Vinícius respondeu: Não consigo agora. 30/09/2026, 14:01").assertExists()
    }

    @Test fun moodAndPersonalSpaceRemainSeparateEntries() {
        val date = LocalDate.of(2026, 9, 30)
        val moodAt = Instant.parse("2026-09-30T14:02:00Z")
        val spaceAt = Instant.parse("2026-09-30T14:03:00Z")
        val entries = listOf(
            IndependentHistoryEntry(
                HistoryEvent.MoodChanged(MoodOption.TIRED, PartnerId.LIDIANNE, moodAt),
                LocalTime.of(14, 2), moodAt, 0, date,
            ),
            IndependentHistoryEntry(
                HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, spaceAt),
                LocalTime.of(14, 3), spaceAt, 1, date,
            ),
        )
        composeRule.setContent {
            DengoTheme {
                HistoryScreen(HistoryUiState(listOf(HistoryDay(date, entries))), PrototypePerspective.LIDIANNE, today = date)
            }
        }

        composeRule.onNodeWithContentDescription("Hoje. 14:02. Lidianne compartilhou como estava. 😴 Cansada").assertExists()
        composeRule.onNodeWithContentDescription("Hoje. 14:03. Lidianne pediu um tempo só dela").assertExists()
    }

    private fun assertRecordedSteps(perspective: PrototypePerspective) {
        val date = LocalDate.of(2026, 9, 30)
        val createdAt = Instant.parse("2026-09-30T14:32:00Z")
        val acceptedAt = Instant.parse("2026-09-30T14:34:00Z")
        val acknowledgedAt = Instant.parse("2026-09-30T18:40:00Z")
        val story = RequestStory(
            requestId = 1,
            requestType = CareRequestType.DENGO,
            requestMessage = null,
            steps = listOf(
                RequestStoryStep(HistoryEvent.RequestCreated(1, PartnerId.LIDIANNE, createdAt), date, LocalTime.of(14, 32)),
                RequestStoryStep(HistoryEvent.RequestAccepted(1, PartnerId.VINICIUS, acceptedAt), date, LocalTime.of(14, 34)),
                RequestStoryStep(HistoryEvent.RequestAcknowledged(1, PartnerId.LIDIANNE, acknowledgedAt), date, LocalTime.of(18, 40)),
            ),
            occurredAt = acknowledgedAt,
            originalIndex = 2,
            date = date,
        )
        composeRule.setContent {
            DengoTheme {
                HistoryScreen(
                    state = HistoryUiState(listOf(HistoryDay(date, listOf(story)))),
                    perspective = perspective,
                    today = date,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Lidianne pediu dengo. 30/09/2026, 14:32").assertExists()
        composeRule.onNodeWithContentDescription("Vinícius respondeu: Estou indo ❤️. 30/09/2026, 14:34").assertExists()
        composeRule.onNodeWithContentDescription("Lidianne agradeceu pelo carinho ❤️. 30/09/2026, 18:40").assertExists()
    }
}
