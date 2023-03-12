package service

import dao.StocksDao
import domain.Company
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*

class StockExchangeService(
    private val stocksDao: StocksDao,
    private val priceGeneratorService: PriceGeneratorService
) {
    fun start(port: Int) {
        embeddedServer(Netty, port) {
            routing {
                get("/company/add") {
                    val name = getParam("company")!!
                    val price = getParam("price")!!.toDouble()
                    val mbStocksCount = getParam("count")?.toInt()

                    if (stocksDao.getCompanyByName(name) == null) {
                        val stocksCount = mbStocksCount ?: 0
                        stocksDao.addCompany(Company(name, price, stocksCount))
                        call.response.status(HttpStatusCode.Created)
                    } else {
                        badRequest("Company $name already exists")
                    }
                }

                get("/stock/add") {
                    val name = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    when (val company = stocksDao.getCompanyByName(name)) {
                        null -> {
                            badRequest("Company $name does not exist")
                        }
                        else -> {
                            stocksDao.updateCompany(
                                company.copy(
                                    stockCount = company.stockCount + count
                                )
                            )
                            call.response.status(HttpStatusCode.OK)
                        }
                    }
                }

                get("/stock/price") {
                    val name = getParam("company")!!
                    val (response, flag) = getValueFromCompanyByName(name) { it.stockPrice.toString() }
                    if (flag == null) {
                        badRequest(response)
                    } else {
                        call.respondText { response }
                    }
                }

                get("/stock/count") {
                    val name = getParam("company")!!
                    val (response, flag) = getValueFromCompanyByName(name) { it.stockCount.toString() }
                    if (flag == null) {
                        badRequest(response)
                    } else {
                        call.respondText { response }
                    }
                }

                get("/stock/buy") {
                    val name = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    val company = stocksDao.getCompanyByName(name)
                    when {
                        company == null -> {
                            badRequest("Company $name does not exist")
                        }
                        company.stockCount < count -> {
                            badRequest("Company $name does not have enough stocks")
                        }
                        else -> {
                            stocksDao.updateCompany(
                                company.copy(stockCount = company.stockCount - count)
                            )
                            call.response.status(HttpStatusCode.OK)
                            priceGeneratorService.generateNewPrice(company)
                            call.respondText("OK")
                        }
                    }
                }

                get("/stock/sell") {
                    val name = getParam("company")!!
                    val count = getParam("count")!!.toInt()

                    when (val company = stocksDao.getCompanyByName(name)) {
                        null -> {
                            badRequest("Company $name does not exist")
                        }
                        else -> {
                            stocksDao.updateCompany(
                                company.copy(stockCount = company.stockCount + count)
                            )
                            call.response.status(HttpStatusCode.OK)
                            priceGeneratorService.generateNewPrice(company)
                            call.respondText("OK")
                        }
                    }
                }

                get("/stock/clear") {
                    stocksDao.clear()
                }
            }
        }.start(wait = true)
    }

    private fun getValueFromCompanyByName(name: String, f: (Company) -> String): Pair<String, String?> {
        val company = stocksDao.getCompanyByName(name)
        return if (company == null) {
            "Company $name does not exist" to "Error"
        } else {
            f(company) to null
        }
    }

    private fun PipelineContext<*, ApplicationCall>.getParam(key: String) = context.request.queryParameters[key]

    private suspend fun PipelineContext<*, ApplicationCall>.badRequest(msg: String) {
        call.response.status(HttpStatusCode.BadRequest)
        call.respondText { msg }
    }
}