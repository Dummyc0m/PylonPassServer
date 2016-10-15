package com.dummyc0m.pylon.pass

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.MailResult

/**
 * http://vertx.io/docs/vertx-mail-client/java/
 * Sample code so no more checking documentation!
 * Created by Dummyc0m on 10/10/16.
 */
class MailBuilder(vertx: Vertx, from: String) {
    private val mailClient: MailClient;
    private val defaultMessage: MailMessage;

    init {
        val config = MailConfig()
//        config.setHostname("mail.example.com")
//        config.setPort(587)
//        config.setStarttls(StartTLSOptions.REQUIRED)
//        config.setUsername("user")
//        config.setPassword("password")
        mailClient = MailClient.createNonShared(vertx, config)
        defaultMessage = MailMessage()
        defaultMessage.from = from

    }

    fun sendMail(mail: MailMessage, handler: (AsyncResult<MailResult>) -> Unit) {
        mailClient.sendMail(mail, handler)
    }

    fun getDefaultMessage(): MailMessage {
        return MailMessage(defaultMessage)
    }
}