package com.viniciusrio.dengo.lab

import com.rabbitmq.client.Channel
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageDeliveryMode
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.slf4j.LoggerFactory

private const val EXCHANGE = "dengo.lab.signals"
private const val QUEUE = "dengo.lab.signals.worker"
private const val ROUTING_KEY = "signal"

fun interface SignalPublisher {
    fun publish(eventId: UUID): Boolean
}

@RestController
@Profile("rabbit-lab")
class SignalController(private val publisher: SignalPublisher) {
    @PostMapping("/lab/signals")
    fun publish(@RequestBody body: Map<String, String>): ResponseEntity<Map<String, String>> {
        val id = body.takeIf { it.keys == setOf("eventId") }
            ?.get("eventId")
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull()
                ?.takeIf { it.toString().equals(value, ignoreCase = true) } }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected only eventId as UUID")
        if (!publisher.publish(id)) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Publication not confirmed and routed")
        }
        return ResponseEntity.accepted().body(mapOf("eventId" to id.toString()))
    }
}

@Configuration
@Profile("rabbit-lab")
class RabbitSignalConfiguration {
    @Bean fun signalExchange() = DirectExchange(EXCHANGE, true, false)
    @Bean fun signalQueue() = Queue(QUEUE, true)
    @Bean fun signalBinding(signalQueue: Queue, signalExchange: DirectExchange): Binding =
        BindingBuilder.bind(signalQueue).to(signalExchange).with(ROUTING_KEY)

    @Bean fun signalPublisher(template: RabbitTemplate): SignalPublisher = RabbitSignalPublisher(template)
    @Bean fun signalProcessor(): SignalProcessor = SignalProcessor { }
}

class RabbitSignalPublisher(private val template: RabbitTemplate, private val routingKey: String = ROUTING_KEY) : SignalPublisher {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun publish(eventId: UUID): Boolean {
        val properties = MessageProperties().apply {
            messageId = eventId.toString()
            contentType = "text/plain"
            deliveryMode = MessageDeliveryMode.PERSISTENT
        }
        val message = Message(eventId.toString().toByteArray(StandardCharsets.UTF_8), properties)
        val correlation = CorrelationData(eventId.toString())
        return try {
            template.send(EXCHANGE, routingKey, message, correlation)
            val confirm = correlation.future.get(5, TimeUnit.SECONDS)
            val accepted = confirm.ack() && correlation.returned == null
            log.info("Signal publish eventId={} brokerAck={} routed={}", eventId, confirm.ack(), correlation.returned == null)
            accepted
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            log.warn("Signal publish interrupted eventId={}", eventId)
            false
        } catch (e: Exception) {
            log.warn("Signal publish inconclusive eventId={} reason={}", eventId, e.javaClass.simpleName)
            false
        }
    }
}

fun interface SignalProcessor {
    fun process(eventId: UUID)
}

@Profile("rabbit-lab")
@Component
class SignalWorker(private val processor: SignalProcessor) {
    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [QUEUE], ackMode = "MANUAL")
    fun receive(message: Message, channel: Channel) {
        val eventId = UUID.fromString(String(message.body, StandardCharsets.UTF_8))
        log.info("Signal delivery attempt eventId={} redelivered={}", eventId, message.messageProperties.isRedelivered)
        try {
            processor.process(eventId)
        } catch (e: Exception) {
            channel.basicNack(message.messageProperties.deliveryTag, false, true)
            log.warn("Signal processing failed; requeued eventId={} reason={}", eventId, e.javaClass.simpleName)
            return
        }
        channel.basicAck(message.messageProperties.deliveryTag, false)
        log.info("Signal processing completed and ACK sent eventId={}", eventId)
    }
}
