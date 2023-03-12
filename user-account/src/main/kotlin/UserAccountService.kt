import client.ExchangeClient
import dao.UserDao
import domain.User
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*

class UserAccountService(
    private val userDao: UserDao,
    private val exchangeClient: ExchangeClient
) {
    fun start(port: Int) {
        embeddedServer(Netty, port) {
            routing {
                get("/user/register") {
                    val name = getParam("name")!!
                    userDao.addUser(
                        User(
                            id = null,
                            name = name,
                            balance = 0.0,
                            stocks = mutableMapOf()
                        )
                    )
                    call.response.status(HttpStatusCode.Created)
                }

                get("/user/deposit") {
                    val id = getParam("id")!!.toLong()
                    val amount = getParam("amount")!!.toDouble()
                    if (amount <= 0) {
                        badRequest("Amount should be positive")
                    } else {
                        when (val user = userDao.getUser(id)) {
                            null -> {
                                badRequest("User with id $id does not exist")
                            }
                            else -> {
                                userDao.updateUser(id, user.copy(balance = user.balance + amount))
                                call.response.status(HttpStatusCode.OK)
                            }
                        }
                    }
                }

                get("/user/withdraw") {
                    val id = getParam("id")!!.toLong()
                    val amount = getParam("amount")!!.toDouble()
                    when {
                        amount <= 0 -> {
                            badRequest("Amount should be positive")
                        }
                        else -> {
                            when (val user = userDao.getUser(id)) {
                                null -> {
                                    badRequest("User with id $id does not exist")
                                }
                                else -> {
                                    if (user.balance < amount) {
                                        badRequest("User with id $id does not have enough balance")
                                    } else {
                                        userDao.updateUser(id, user.copy(balance = user.balance - amount))
                                        call.response.status(HttpStatusCode.OK)
                                    }
                                }
                            }
                        }
                    }
                }

                get("/user/stocks") {
                    val id = getParam("id")!!.toLong()

                    when (val user = userDao.getUser(id)) {
                        null -> {
                            badRequest("User with id $id does not exist")
                        }
                        else -> {
                            call.respondText { user.stocks.toList().joinToString { "${it.first}:${it.second}" } }
                            call.response.status(HttpStatusCode.OK)
                        }
                    }
                }

                get("/user/total") {
                    val id = getParam("id")!!.toLong()

                    when (val user = userDao.getUser(id)) {
                        null -> {
                            badRequest("User with id $id does not exist")
                        }
                        else -> {
                            val total = user.stocks.map { (companyName, count) ->
                                val price = exchangeClient.getPrice(companyName)
                                count * price
                            }.sum()
                            call.respondText { total.toString() }
                        }
                    }
                }

                get("/user/balance") {
                    val id = getParam("id")!!.toLong()

                    when (val user = userDao.getUser(id)) {
                        null -> {
                            badRequest("User with id $id does not exist")
                        }
                        else -> {
                            call.respondText { user.balance.toString() }
                        }
                    }
                }

                get("/user/buy") {
                    val id = getParam("id")!!.toLong()
                    val company = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    when (val user = userDao.getUser(id)) {
                        null -> {
                            badRequest("User with id $id does not exist")
                        }
                        else -> {
                            val price = exchangeClient.getPrice(company)
                            if (user.balance < price * count) {
                                badRequest("User with id $id does not have enough balance")
                            } else {
                                val response = exchangeClient.buy(company, count)
                                if (response == "OK") {
                                    user.stocks.putIfAbsent(company, 0)
                                    user.stocks[company] = user.stocks[company]!! + count
                                    userDao.updateUser(
                                        id, user.copy(
                                            balance = user.balance - price * count
                                        )
                                    )
                                } else {
                                    badRequest(response)
                                }
                            }
                        }
                    }
                }

                get("/user/sell") {
                    val id = getParam("id")!!.toLong()
                    val company = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    when (val user = userDao.getUser(id)) {
                        null -> {
                            badRequest("User with id $id does not exist")
                        }
                        else -> {
                            when (val response = exchangeClient.sell(company, count)) {
                                "OK" -> {
                                    if (user.stocks.getOrDefault(company, 0) < count) {
                                        badRequest("User with id $id does not have enough stocks")
                                    } else {
                                        user.stocks[company] = user.stocks[company]!! - count
                                        val price = exchangeClient.getPrice(company)
                                        userDao.updateUser(
                                            id, user.copy(
                                                balance = user.balance + price * count
                                            )
                                        )
                                    }
                                }
                                else -> {
                                    badRequest(response)
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = true)
    }

    private fun PipelineContext<*, ApplicationCall>.getParam(key: String) = context.request.queryParameters[key]

    private suspend fun PipelineContext<*, ApplicationCall>.badRequest(msg: String) {
        call.response.status(HttpStatusCode.BadRequest)
        call.respondText { msg }
    }
}