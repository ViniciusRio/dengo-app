package com.viniciusrio.dengo.ui.history

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val now = Instant.parse("2026-09-30T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialStateIsEmpty() = runTest {
        val viewModel = HistoryViewModel(FakeCoupleRepository(clock), clock)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.days.isEmpty())
    }

    @Test
    fun requestAndResponseAppearAsOneHistoryEntry() {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO)
        repository.acceptRequest(request.id)

        val entries = repository.state.value.toHistoryUiState(clock.zone).days.single().entries
        assertEquals(1, entries.size)
        val story = entries.single() as RequestStory
        assertEquals(request.id, story.requestId)
        assertEquals(CareRequestType.DENGO, story.requestType)
        assertEquals(
            listOf(HistoryEvent.RequestCreated::class, HistoryEvent.RequestAccepted::class),
            story.steps.map { it.event::class },
        )
    }

    @Test
    fun sameTypeAndSameOtherTextStillProduceSeparateStoriesByRequestId() {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createRequest(CareRequestType.OTHER, " Um abraço ")
        val second = repository.createRequest(CareRequestType.OTHER, "Um abraço")
        repository.acceptRequest(second.id)

        val stories = repository.state.value.toHistoryUiState(clock.zone).days.single().entries
            .map { it as RequestStory }
        assertEquals(listOf(second.id, first.id), stories.map { it.requestId })
        assertEquals(listOf(2, 1), stories.map { it.steps.size })
        assertEquals(listOf("Um abraço", "Um abraço"), stories.map { it.requestMessage })
        assertEquals(3, stories.sumOf { it.steps.size })
    }

    @Test
    fun missingRequestKeepsAllRecordedStepsWithGenericContext() {
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.RequestCreated(42, PartnerId.LIDIANNE, now),
                HistoryEvent.RequestAccepted(42, PartnerId.VINICIUS, now),
                HistoryEvent.RequestDeclined(43, PartnerId.VINICIUS, now),
            ),
        )

        val stories = state.toHistoryUiState(clock.zone).days.single().entries.map { it as RequestStory }
        assertEquals(listOf(43L, 42L), stories.map { it.requestId })
        assertEquals(listOf(1, 2), stories.map { it.steps.size })
        stories.forEach {
            assertNull(it.requestType)
            assertNull(it.requestMessage)
        }
    }

    @Test
    fun latestRecordedStepAnchorsStoryAcrossLocalDays() {
        val zone = ZoneId.of("America/Fortaleza")
        val created = Instant.parse("2026-09-30T02:30:00Z") // 29/09, 23:30
        val accepted = Instant.parse("2026-09-30T03:15:00Z") // 30/09, 00:15
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.RequestCreated(7, PartnerId.LIDIANNE, created),
                HistoryEvent.MoodChanged(MoodOption.TIRED, PartnerId.LIDIANNE, Instant.parse("2026-09-30T02:45:00Z")),
                HistoryEvent.RequestAccepted(7, PartnerId.VINICIUS, accepted),
            ),
        )

        val days = state.toHistoryUiState(zone).days
        assertEquals(listOf(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 29)), days.map { it.date })
        val story = days.first().entries.single() as RequestStory
        assertEquals(listOf(LocalDate.of(2026, 9, 29), LocalDate.of(2026, 9, 30)), story.steps.map { it.date })
        assertEquals(listOf(LocalTime.of(23, 30), LocalTime.of(0, 15)), story.steps.map { it.time })
        assertEquals(HistoryEvent.MoodChanged::class, (days.last().entries.single() as IndependentHistoryEntry).event::class)
    }

    @Test
    fun storiesAndIndependentEventsSortByLatestInstantThenInsertionIndex() {
        val earlier = now.minusSeconds(60)
        val later = now.plusSeconds(60)
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.RequestCreated(7, PartnerId.LIDIANNE, earlier),
                HistoryEvent.MoodChanged(MoodOption.TIRED, PartnerId.LIDIANNE, now),
                HistoryEvent.RequestAccepted(7, PartnerId.VINICIUS, now),
                HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, now),
                HistoryEvent.PersonalSpaceEnded(PartnerId.LIDIANNE, later),
            ),
        )

        val entries = state.toHistoryUiState(clock.zone).days.single().entries
        assertEquals(listOf(4, 3, 2, 1), entries.map { it.originalIndex })
        assertEquals(
            listOf(IndependentHistoryEntry::class, IndependentHistoryEntry::class,
                RequestStory::class, IndependentHistoryEntry::class),
            entries.map { it::class },
        )
    }

    @Test
    fun moodChangesAndPersonalSpaceRemainIndividualWithoutJoiningRequestStory() {
        val repository = FakeCoupleRepository(clock)
        repository.createRequest(CareRequestType.DENGO)
        repository.setMood(MoodOption.TIRED)
        repository.setMood(MoodOption.HAPPY)
        repository.activatePersonalSpace()
        repository.endPersonalSpace()

        val entries = repository.state.value.toHistoryUiState(clock.zone).days.single().entries
        assertEquals(1, entries.count { it is RequestStory })
        assertEquals(4, entries.count { it is IndependentHistoryEntry })
        assertEquals(5, entries.size)
        assertEquals(5, entries.sumOf { if (it is RequestStory) it.steps.size else 1 })
    }

    @Test
    fun currentRequestStatusDoesNotRewriteEarlierEvents() {
        val request = CareRequest(
            id = 8, type = CareRequestType.DENGO, requesterId = PartnerId.LIDIANNE,
            recipientId = PartnerId.VINICIUS, status = CareRequestStatus.ACKNOWLEDGED, createdAt = now,
        )
        val state = CoupleState(
            partners = emptyList(), requests = listOf(request),
            history = listOf(
                HistoryEvent.RequestCreated(8, PartnerId.LIDIANNE, now),
                HistoryEvent.RequestAccepted(8, PartnerId.VINICIUS, now),
            ),
        )

        val story = state.toHistoryUiState(clock.zone).days.single().entries.single() as RequestStory
        assertEquals(2, story.steps.size)
        assertEquals(HistoryEvent.RequestAccepted::class, story.steps.last().event::class)
    }

    @Test
    fun acknowledgementIsOnlyShownWhenItsEventExists() {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO)
        repository.acceptRequest(request.id)
        val before = repository.state.value.toHistoryUiState(clock.zone).days.single().entries.single() as RequestStory
        assertEquals(2, before.steps.size)

        repository.acknowledgeRequest(request.id, PartnerId.LIDIANNE)
        val after = repository.state.value.toHistoryUiState(clock.zone).days.single().entries.single() as RequestStory
        assertEquals(
            listOf(HistoryEvent.RequestCreated::class, HistoryEvent.RequestAccepted::class,
                HistoryEvent.RequestAcknowledged::class),
            after.steps.map { it.event::class },
        )
    }

    @Test
    fun absentCreationAndContradictoryEventsRemainVisibleWithoutInventedSteps() {
        val state = CoupleState(
            partners = emptyList(),
            history = listOf(
                HistoryEvent.RequestAccepted(9, PartnerId.VINICIUS, now),
                HistoryEvent.RequestDeclined(9, PartnerId.VINICIUS, now),
                HistoryEvent.RequestAcknowledged(9, PartnerId.LIDIANNE, now),
            ),
        )

        val story = state.toHistoryUiState(clock.zone).days.single().entries.single() as RequestStory
        assertEquals(
            listOf(HistoryEvent.RequestAccepted::class, HistoryEvent.RequestDeclined::class,
                HistoryEvent.RequestAcknowledged::class),
            story.steps.map { it.event::class },
        )
    }

    @Test
    fun bothPerspectivesSeeSameReadOnlyProjection() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        val history = HistoryViewModel(repository, clock)
        lidianne.createQuickRequest(CareRequestType.DENGO)
        val requestId = repository.state.value.requests.single().id
        vinicius.acceptRequest(requestId)
        lidianne.setMood(MoodOption.LOVING)
        val before = repository.state.value
        advanceUntilIdle()

        val entries = history.state.value.days.single().entries
        assertEquals(listOf(IndependentHistoryEntry::class, RequestStory::class), entries.map { it::class })
        assertEquals(3, entries.sumOf { if (it is RequestStory) it.steps.size else 1 })
        assertEquals(before, repository.state.value)
    }

    @Test
    fun dayAndTimeLabelsRemainLocal() {
        val today = LocalDate.of(2026, 9, 30)
        assertEquals(HistoryDayLabel.TODAY, historyDayLabel(today, today))
        assertEquals(HistoryDayLabel.YESTERDAY, historyDayLabel(today.minusDays(1), today))
        assertEquals(HistoryDayLabel.DATE, historyDayLabel(today.minusDays(2), today))
        assertEquals("28/09/2026", formatHistoryDate(today.minusDays(2)))
        assertEquals("09:05", formatHistoryTime(LocalTime.of(9, 5)))
    }
}
