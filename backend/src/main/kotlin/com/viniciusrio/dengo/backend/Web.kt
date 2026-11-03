package com.viniciusrio.dengo.backend

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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

internal const val SESSION_ATTRIBUTE = "dengo.session"

data class ApiError(val code: String)

internal fun bearerToken(request: HttpServletRequest): String =
    request.getHeader(HttpHeaders.AUTHORIZATION)
        ?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")?.trim()?.takeIf(String::isNotEmpty)
        ?: throw SessionRefused("AUTH_REQUIRED")

// Protege toda a API por padrão. Ficam fora o vínculo, que é público, e a sincronização, que autentica
// dentro do próprio retrato do banco. As escritas conferem de novo a sessão em `Operations`.
@Component
class SessionInterceptor(private val identity: Identity) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val session = identity.sessionForToken(bearerToken(request)) ?: throw SessionRefused("SESSION_REVOKED")
        request.setAttribute(SESSION_ATTRIBUTE, session)
        return true
    }
}

@Component
class WebConfiguration(private val sessions: SessionInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sessions).addPathPatterns("/v1/**").excludePathPatterns("/v1/pairings", "/v1/sync")
    }
}

/** Respostas sem eco do corpo recebido: o log padrão dessas falhas poderia trazer conteúdo afetivo. */
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(SessionRefused::class)
    fun sessionRefused(refused: SessionRefused) = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError(refused.code))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun malformedBody() = ResponseEntity.badRequest().body(ApiError("MALFORMED_BODY"))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun malformedParameter() = ResponseEntity.badRequest().body(ApiError("MALFORMED_REQUEST"))

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun missingHeader() = ResponseEntity.badRequest().body(ApiError("IDEMPOTENCY_KEY_REQUIRED"))
}
