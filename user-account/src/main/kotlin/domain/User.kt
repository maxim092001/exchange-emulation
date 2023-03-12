package domain

data class User(
    val id: Long?,
    val name: String,
    val balance: Double,
    val stocks: MutableMap<String, Int>
)