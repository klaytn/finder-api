package io.klaytn.finder.infra.error

import com.fasterxml.jackson.databind.ObjectMapper
import io.klaytn.commons.utils.logback.logger
import io.klaytn.finder.infra.exception.ApplicationErrorException
import io.klaytn.finder.infra.exception.InternalServerErrorException
import io.klaytn.finder.infra.exception.InvalidRequestException
import io.klaytn.finder.infra.exception.NotFoundUrlException
import org.eclipse.jetty.io.EofException
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.view.json.MappingJackson2JsonView
import java.io.EOFException
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ExceptionHandler(
    private val messageSource: MessageSource,
    private val objectMapper: ObjectMapper,
) {
    private val logger = logger(this::class.java)
    private val jsonView = MappingJackson2JsonView(objectMapper)

    /**
     * @see DefaultHandlerExceptionResolver
     */
    fun getErrorResultView(
        request: HttpServletRequest,
        throwable: Throwable,
    ): ModelAndView {
        if(throwable is EofException || throwable is EOFException) {
            return ModelAndView()
        }

        val errorResponse = getErrorResponse(request, throwable)
        val modelAndView = ModelAndView(jsonView, getErrorResponseMap(errorResponse))
        modelAndView.status = errorResponse.httpStatus
        return modelAndView
    }

    /**
     * @see DefaultExceptionHandlerFilter
     */
    fun writeErrorResponse(request: HttpServletRequest, response: HttpServletResponse, throwable: Throwable) {
        try {
            val errorResponse = getErrorResponse(request, throwable)

            val bytes = objectMapper.writeValueAsBytes(getErrorResponseMap(errorResponse))
            response.status = errorResponse.httpStatus.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.setContentLength(bytes.size)
            response.outputStream.use { out -> out.write(bytes) }
        } catch (e: IOException) {
            logger.warn("Fail to write error message", e)
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -- private
    // -----------------------------------------------------------------------------------------------------------------

    private fun getErrorResponse(
        request: HttpServletRequest,
        ex: Throwable,
    ): ApplicationErrorResponse {
        return when (ex) {
            is IllegalArgumentException -> createErrorResponse(request, InvalidRequestException(ex))
            is HttpRequestMethodNotSupportedException -> createErrorResponse(request, InvalidRequestException(ex))
            is MethodArgumentTypeMismatchException -> createErrorResponse(request, InvalidRequestException(ex))
            is NoHandlerFoundException -> createErrorResponse(request, NotFoundUrlException(ex, request))
            is BindException -> createErrorResponse(request, InvalidRequestException(ex))
            is ApplicationErrorException -> createErrorResponse(request, ex)
            else -> createErrorResponse(request, InternalServerErrorException(ex))
        }
    }

    private fun createErrorResponse(
        httpServletRequest: HttpServletRequest,
        applicationException: ApplicationErrorException,
    ): io.klaytn.finder.infra.error.ApplicationErrorResponse {
        val locale = httpServletRequest.locale
        val errorType = applicationException.applicationErrorType

        val errorTitleKey = "error.${errorType.name}.title".lowercase()
        val errorTitle = messageSource.getMessage(
            errorTitleKey, null, errorType.title, locale
        )

        val errorMessageKey = "error.${errorType.name}.message".lowercase()
        val errorMessage = messageSource.getMessage(
            errorMessageKey, applicationException.messageArguments.toArray(), errorType.message, locale
        )

        val cause: Throwable = applicationException.cause ?: applicationException
        log(errorType.statusCode, httpServletRequest, cause)

        return io.klaytn.finder.infra.error.ApplicationErrorResponse(errorType, errorTitle, errorMessage)
    }

    private fun getErrorResponseMap(applicationErrorResponse: io.klaytn.finder.infra.error.ApplicationErrorResponse) =
        mapOf(
            "code" to applicationErrorResponse.code,
            "title" to applicationErrorResponse.title,
            "message" to applicationErrorResponse.message
        )

    private fun log(statusCode: HttpStatus, request: HttpServletRequest, t: Throwable) {
        val dump = dumpRequest(request)
        if (statusCode.is5xxServerError) {
            logger.error("Exception occurred. {}\n{}", t.message, dump, t)
        } else {
            logger.debug("Exception occurred. {}\n{}", t.message, dump, t)
        }
    }

    private fun dumpRequest(request: HttpServletRequest): String {
        val sb =
            StringBuilder().append("Request: ").append(request.method).append(" ").append(request.requestURI)
                .append("\n")
                .append("Query: ").append(request.queryString).append("\n")
                .append("Headers\n")
        request.headerNames.asIterator().forEachRemaining { name ->
            sb.append("\t").append(name).append(": ").append(request.getHeader(name)).append("\n")
        }
        return sb.toString()
    }
}
