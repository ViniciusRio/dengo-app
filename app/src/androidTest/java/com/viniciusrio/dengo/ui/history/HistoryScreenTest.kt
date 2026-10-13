package com.viniciusrio.dengo.ui.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.PartnerId
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

    @Test fun requestStoryReadsCreationResponseAndAcknowledgementInOrder() {
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
            DengoTheme { HistoryScreen(HistoryUiState(listOf(HistoryDay(date, listOf(story)))), today = date) }
        }

        composeRule.onNodeWithContentDescription("Lidianne pediu dengo. 30/09/2026, 14:32").assertExists()
        composeRule.onNodeWithContentDescription("Vinícius respondeu: Estou indo ❤️. 30/09/2026, 14:34").assertExists()
        composeRule.onNodeWithContentDescription("Lidianne agradeceu pelo carinho ❤️. 30/09/2026, 18:40").assertExists()
    }
}
