package com.dummyc0m.pylon.pass

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

fun main(args: Array<String>) {
    val port = 8081
    val options = DeploymentOptions().setConfig(JsonObject().put("http.port", port))
    Vertx.vertx().deployVerticle(PylonPassVerticle::class.java.getName(), options)
}

// form
//        router.post("/PylonPass/*").handler { ctx ->
//            ctx.request().isExpectMultipart = true
//            ctx.request().endHandler { event ->
//                ctx.next()
//            }
//        }