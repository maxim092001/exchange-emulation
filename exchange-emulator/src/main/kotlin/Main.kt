import dao.StocksInMemoryDao
import service.PriceGeneratorService
import service.StockExchangeService
import java.util.*

fun main(args: Array<String>) {
    val stocksDao = StocksInMemoryDao()
    StockExchangeService(stocksDao, PriceGeneratorService(stocksDao, Random()))
        .start(args[0].toInt())
}