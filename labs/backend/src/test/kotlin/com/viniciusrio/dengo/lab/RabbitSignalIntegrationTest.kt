package com.viniciusrio.dengo.lab

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Tag("rabbit-integration")
@SpringBootTest(properties = ["spring.profiles.active=rabbit-lab"])
@AutoConfigureMockMvc
@Import(RabbitSignalIntegrationTest.TestProcessorConfiguration::class)
class RabbitSignalIntegrationTest(
    @param:Autowired private val mvc: MockMvc,
    @param:Autowired private val template: RabbitTemplate,
    @param:Autowired private val listeners: RabbitListenerEndpointRegistry,
    @param:Autowired private val probe: ProcessingProbe,
) {
    @Test fun confirmedPublishAndManualAckAfterRedelivery() {
        val eventId = UUID.randomUUID()
        probe.failFirstFor = eventId
        mvc.perform(post("/lab/signals").contentType(MediaType.APPLICATION_JSON)
            .content("""{"eventId":"$eventId"}"""))
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.eventId").value(eventId.toString()))
        assertTrue(probe.completed.await(10, TimeUnit.SECONDS), "worker did not complete after redelivery")
        assertEquals(2, probe.attempts[eventId])
        assertEquals(1, probe.successes[eventId])
        assertEquals(false, RabbitSignalPublisher(template, "missing-route").publish(UUID.randomUUID()))
        listeners.stop()
        assertEquals(0, queueMessageCount(), "stopping the consumer must not requeue an acknowledged signal")
    }

    private fun queueMessageCount(): Int = template.execute { channel ->
        channel.queueDeclarePassive("dengo.lab.signals.worker").messageCount
    } ?: -1

    @TestConfiguration
    class TestProcessorConfiguration {
        @Bean fun processingProbe() = ProcessingProbe()
        @Bean @Primary fun testSignalProcessor(probe: ProcessingProbe): SignalProcessor = SignalProcessor { id ->
            val attempt = probe.attempts.merge(id, 1, Int::plus)!!
            if (id == probe.failFirstFor && attempt == 1) throw IllegalStateException("injected before ACK")
            probe.successes.merge(id, 1, Int::plus)
            if (id == probe.failFirstFor) probe.completed.countDown()
        }
    }
}

class ProcessingProbe {
    @Volatile var failFirstFor: UUID? = null
    val attempts = ConcurrentHashMap<UUID, Int>()
    val successes = ConcurrentHashMap<UUID, Int>()
    val completed = CountDownLatch(1)
}
