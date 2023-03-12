package service

import dao.StocksDao
import domain.Company
import java.util.*
import kotlin.math.sign

class PriceGeneratorService(
    private val stocksDao: StocksDao,
    private val random: Random
) {
    fun generateNewPrice(company: Company) {
        val sign = sign(random.nextInt().toDouble())
        val priceDelta = sign * company.stockPrice / 10
        stocksDao.updateCompany(
            company.copy(stockPrice = company.stockPrice + priceDelta)
        )
    }
}