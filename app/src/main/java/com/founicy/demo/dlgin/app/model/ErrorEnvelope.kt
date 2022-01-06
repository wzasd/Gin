package com.founicy.demo.dlgin.app.model

/**
 * ErrorEnvelope is a custom error model for handling network error.
 *
 * @param code Http error response code.
 * @param message Http error response body message.
 */
data class ErrorEnvelope(
  val code: Int,
  val message: String
)
