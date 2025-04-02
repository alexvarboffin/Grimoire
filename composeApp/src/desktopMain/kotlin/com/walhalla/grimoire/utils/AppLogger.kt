package com.walhalla.grimoire.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AppLogger {
    private val logger: Logger = LoggerFactory.getLogger("GrimoireApp")

    fun debug(message: String) = logger.debug(message)
    fun info(message: String) = logger.info(message)
    fun warn(message: String) = logger.warn(message)
    fun error(message: String) = logger.error(message)
    fun error(message: String, throwable: Throwable) = logger.error(message, throwable)
}