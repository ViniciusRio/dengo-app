package com.viniciusrio.dengo.data.sync

import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import java.time.Instant
import org.json.JSONObject

/** Os nomes de acontecimento são os mesmos no servidor e na cópia local. */
internal fun historyEvent(kind: String, actor: PartnerId, at: Instant, requestId: Long?, moodOption: String?): HistoryEvent =
    when (kind) {
        "REQUEST_CREATED" -> HistoryEvent.RequestCreated(requireNotNull(requestId), actor, at)
        "REQUEST_ACCEPTED" -> HistoryEvent.RequestAccepted(requireNotNull(requestId), actor, at)
        "REQUEST_DECLINED" -> HistoryEvent.RequestDeclined(requireNotNull(requestId), actor, at)
        "REQUEST_ACKNOWLEDGED" -> HistoryEvent.RequestAcknowledged(requireNotNull(requestId), actor, at)
        "MOOD_CHANGED" -> HistoryEvent.MoodChanged(MoodOption.valueOf(requireNotNull(moodOption)), actor, at)
        "PERSONAL_SPACE_ACTIVATED" -> HistoryEvent.PersonalSpaceActivated(actor, at)
        "PERSONAL_SPACE_ENDED" -> HistoryEvent.PersonalSpaceEnded(actor, at)
        else -> throw IllegalArgumentException("Unknown history event kind: $kind")
    }

internal val HistoryEvent.kind: String
    get() = when (this) {
        is HistoryEvent.RequestCreated -> "REQUEST_CREATED"
        is HistoryEvent.RequestAccepted -> "REQUEST_ACCEPTED"
        is HistoryEvent.RequestDeclined -> "REQUEST_DECLINED"
        is HistoryEvent.RequestAcknowledged -> "REQUEST_ACKNOWLEDGED"
        is HistoryEvent.MoodChanged -> "MOOD_CHANGED"
        is HistoryEvent.PersonalSpaceActivated -> "PERSONAL_SPACE_ACTIVATED"
        is HistoryEvent.PersonalSpaceEnded -> "PERSONAL_SPACE_ENDED"
    }

internal val HistoryEvent.requestId: Long?
    get() = when (this) {
        is HistoryEvent.RequestCreated -> requestId
        is HistoryEvent.RequestAccepted -> requestId
        is HistoryEvent.RequestDeclined -> requestId
        is HistoryEvent.RequestAcknowledged -> requestId
        else -> null
    }

internal fun PendingCommand.encode(): String {
    val json = JSONObject()
    when (this) {
        is PendingCommand.CreateRequest -> json.put("command", "createRequest").put("type", type.name).putOpt("message", message)
        is PendingCommand.AcceptRequest -> json.put("command", "accept").put("requestId", requestId)
        is PendingCommand.DeclineRequest -> json.put("command", "decline").put("requestId", requestId)
        is PendingCommand.AcknowledgeRequest -> json.put("command", "acknowledge").put("requestId", requestId)
        is PendingCommand.SetMood -> json.put("command", "setMood").put("option", option.name)
        PendingCommand.ActivatePersonalSpace -> json.put("command", "activatePersonalSpace")
        PendingCommand.EndPersonalSpace -> json.put("command", "endPersonalSpace")
    }
    return json.toString()
}

internal fun decodePendingCommand(text: String): PendingCommand {
    val json = JSONObject(text)
    return when (val command = json.getString("command")) {
        "createRequest" -> PendingCommand.CreateRequest(
            CareRequestType.valueOf(json.getString("type")),
            if (json.isNull("message")) null else json.getString("message"),
        )
        "accept" -> PendingCommand.AcceptRequest(json.getLong("requestId"))
        "decline" -> PendingCommand.DeclineRequest(json.getLong("requestId"))
        "acknowledge" -> PendingCommand.AcknowledgeRequest(json.getLong("requestId"))
        "setMood" -> PendingCommand.SetMood(MoodOption.valueOf(json.getString("option")))
        "activatePersonalSpace" -> PendingCommand.ActivatePersonalSpace
        "endPersonalSpace" -> PendingCommand.EndPersonalSpace
        else -> throw IllegalArgumentException("Unknown pending command: $command")
    }
}
