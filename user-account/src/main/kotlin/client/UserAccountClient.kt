package client

class UserAccountClient(baseUrl: String) : BaseHttpClient(baseUrl) {
    fun registerUser(name: String) {
        req(
            "/user/register", mutableMapOf(
                "name" to name
            )
        )
    }

    fun deposit(userId: Long, amount: Double) {
        req(
            "/user/deposit", mutableMapOf(
                "id" to userId.toString(),
                "amount" to amount.toString()
            )
        )
    }

    fun withdraw(userId: Long, amount: Double) {
        req(
            "/user/withdraw", mutableMapOf(
                "id" to userId.toString(),
                "amount" to amount.toString()
            )
        )
    }

    fun getStocks(userId: Long): String {
        return req(
            "/user/stocks", mutableMapOf(
                "id" to userId.toString()
            )
        )
    }

    fun getTotal(userId: Long): Double {
        return req(
            "/user/total", mutableMapOf(
                "id" to userId.toString()
            )
        ).toDouble()
    }

    fun getBalance(userId: Long): Double {
        return req(
            "/user/balance", mutableMapOf(
                "id" to userId.toString()
            )
        ).toDouble()
    }

    fun buy(userId: Long, company: String, count: Int): String {
        return req(
            "/user/buy", mutableMapOf(
                "id" to userId.toString(),
                "company" to company,
                "count" to count.toString()
            )
        )
    }

    fun sell(userId: Long, company: String, count: Int): String {
        return req(
            "/user/sell", mutableMapOf(
                "id" to userId.toString(),
                "company" to company,
                "count" to count.toString()
            )
        )
    }
}