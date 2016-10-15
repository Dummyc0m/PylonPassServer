package com.dummyc0m.pylon.pass

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.auth.mongo.MongoAuth
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.JWTAuthHandler

class PylonPassVerticle : AbstractVerticle() {
    override fun start(future: Future<Void>) {
        val router = Router.router(vertx)
        val jwt = JWTAuth.create(vertx, JsonObject().put("keyStore", JsonObject().put("type", "jceks")
                .put("path", "keystore.jceks")
                .put("password", "secret")))

        var uri = config().getString("mongo_uri")
        if (uri == null) {
            uri = "mongodb://localhost:27017"
        }

        var db = config().getString("mongo_db")
        if (db == null) {
            db = "test"
        }

        val mongoClient = MongoClient.createShared(vertx, JsonObject()
                .put("connection_string", uri)
                .put(db, "demo"))

        val mongoAuth = MongoAuth.create(mongoClient, JsonObject())

        // preflight handler
        router.options("/PylonPass/*").handler { ctx ->
            ctx.response().putHeader("Access-Control-Allow-Origin", "http://localhost:8080")
            ctx.response().putHeader("Access-Control-Allow-Headers", "Authorization, Content-Type")
            ctx.response().putHeader("Access-Control-Allow-Methods", "GET, POST")
            ctx.response().end()
        }

        router.route("/Pylon/*").handler(JWTAuthHandler.create(jwt))

        // fall over handler
        router.route("/PylonPass/*").handler { ctx ->
            ctx.response().putHeader("Access-Control-Allow-Origin", "http://localhost:8080")
            ctx.next()
        }

        router.post("/PylonPass/authenticate").handler(BodyHandler.create())

        router.post("/PylonPass/authenticate").handler { ctx ->
            try {
                val bodyAsJson = ctx.bodyAsJson;
                val username = bodyAsJson?.getString("username")
                val password = bodyAsJson?.getString("password")
                println(bodyAsJson)
                if (!(username.isNullOrEmpty() || password.isNullOrEmpty())) {
                    mongoAuth.authenticate(JsonObject().put("username", username).put("password", password)) { res ->
                        if (res.succeeded()) {
                            val principal = res.result().principal()
                            val e = principal.getJsonArray(mongoAuth.permissionField)
                            val permission = JWTOptions().setExpiresInSeconds(3600L)
                            e.forEach { element ->
                                permission.addPermission(element.toString())
                            }
                            ctx.response().putHeader("Content-Type", "text/plain")
                            ctx.response().end(jwt.generateToken(JsonObject().put("username", username), permission))
                        } else {
                            ctx.fail(401)
                        }
                    }
                } else {
                    ctx.fail(401)
                }
            } catch (e: Exception) {
                ctx.fail(401)
            }
        }

        router.post("/PylonPass/register").handler(BodyHandler.create())

        router.post("/PylonPass/register").handler { ctx ->
            val bodyAsJson = ctx.bodyAsJson;
            val username = bodyAsJson?.getString("username")
            val password = bodyAsJson?.getString("password")
            println(bodyAsJson)
            if (!(username.isNullOrEmpty() || password.isNullOrEmpty())) {
                mongoAuth.insertUser(username, password, arrayListOf(), arrayListOf("Tumperi")) { res ->
                    if (res.succeeded()) {
                        mongoAuth.authenticate(JsonObject().put("username", username).put("password", password)) { res ->
                            if (res.succeeded()) {
                                val principal = res.result().principal()
                                val e = principal.getJsonArray(mongoAuth.permissionField)
                                val permission = JWTOptions().setExpiresInSeconds(3600L)
                                e.forEach { element ->
                                    permission.addPermission(element.toString())
                                }
                                ctx.response().putHeader("Content-Type", "text/plain")
                                ctx.response().end(jwt.generateToken(JsonObject().put("username", username), permission))
                            } else {
                                ctx.fail(401)
                            }
                        }
                    } else {
                        ctx.fail(res.cause())
                    }
                }
            }
        }

        router.get("/Pylon/connect").handler { ctx ->
            ctx.user().isAuthorised("Tumperi") { isAuthorized ->
                if (isAuthorized.succeeded() && isAuthorized.result()) {
                    ctx.response().putHeader("Content-Type", "text/plain")
                    ctx.response().end("You are online")
                } else {
                    ctx.fail(401)
                }
            }
        }

        vertx.createHttpServer().requestHandler({ req -> router.accept(req) }).listen(7080);
    }
}