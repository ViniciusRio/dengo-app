package com.viniciusrio.dengo.backend

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Component
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

internal const val ACTOR_ATTRIBUTE = "dengo.actor"

data class ApiError(val code: String)

// Protege toda a API por padrão: só o vínculo fica fora.
@Component
class SessionInterceptor(private val identity: Identity) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        val token = header?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")?.trim()?.takeIf(String::isNotEmpty)
            ?: return refuse(response, "AUTH_REQUIRED")
        val person = identity.personForToken(token) ?: return refuse(response, "SESSION_REVOKED")
        request.setAttribute(ACTOR_ATTRIBUTE, person)
        return true
    }

    private fun refuse(response: HttpServletResponse, code: String): Boolean {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write("""{"code":"$code"}""")
        return false
    }
}

@Component
class WebConfiguration(private val sessions: SessionInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sessions).addPathPatterns("/v1/**").excludePathPatterns("/v1/pairings")
    }
}

/** Respostas sem eco do corpo recebido: o log padrão dessas falhas poderia trazer conteúdo afetivo. */
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun malformedBody() = ResponseEntity.badRequest().body(ApiError("MALFORMED_BODY"))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun malformedParameter() = ResponseEntity.badRequest().body(ApiError("MALFORMED_REQUEST"))

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun missingHeader() = ResponseEntity.badRequest().body(ApiError("IDEMPOTENCY_KEY_REQUIRED"))
}
