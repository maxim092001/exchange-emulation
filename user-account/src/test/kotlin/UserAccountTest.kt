import client.ExchangeClient
import client.UserAccountClient
import dao.UserInMemoryDao
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserAccountTest {

    private val exchangeContainer = ExchangeContainer(EXCHANGE_PORT)

    private val exchangeClient = ExchangeClient("http://localhost:$EXCHANGE_PORT")
    private val userAccountClient = UserAccountClient("http://localhost:$USER_ACCOUNT_PORT")

    private val userDao = UserInMemoryDao()
    private val userAccountService = UserAccountService(userDao, exchangeClient)

    @BeforeAll
    fun beforeAll() {
        exchangeContainer.start()
        userAccountService.start(USER_ACCOUNT_PORT)
    }

    @AfterAll
    fun afterAll() {
        exchangeContainer.stop()
    }

    @BeforeEach
    fun beforeEach() {
        exchangeClient.clear()
        userDao.clear()
    }

    @Test
    fun depositTest_negativeAmount_resultFailure() {
        userAccountClient.registerUser("user")
        val response = userAccountClient.deposit(0, -100.0)
        assertEquals("Amount should be positive", response)
    }

    @Test
    fun buyTest_userNotExist_resultFailure() {
        exchangeClient.addCompany("company", 10, 10.0)
        val response = userAccountClient.buy(0, "company", 1)
        assertEquals("User with id 0 does not exist", response)
    }

    @Test
    fun buyTest_hasEnoughBalance_resultSuccess() {
        exchangeClient.addCompany("company", 10, 10.0)

        userAccountClient.registerUser("user")
        userAccountClient.deposit(0, 100.0)

        val response = userAccountClient.buy(0, "company", 10)
        assertEquals("OK", response)

        val balanceAfter = userAccountClient.getBalance(0)
        val totalAfter = userAccountClient.getTotal(0)

        assertEquals(0, balanceAfter)
        assertEquals(100, totalAfter)
    }

    @Test
    fun buyTest_notEnoughBalance_resultFailure() {
        exchangeClient.addCompany("company", 10, 10.0)
        userAccountClient.registerUser("user")
        val response = userAccountClient.buy(0, "company", 1)
        assertEquals("User with id 0 does not have enough balance", response)
    }

    @Test
    fun sellTest_noStocks_resultFailure() {
        exchangeClient.addCompany("company", 10, 10.0)
        userAccountClient.registerUser("user")
        val response = userAccountClient.sell(0, "company", 1)
        assertEquals("User with id 0 does not have enough stocks", response)
    }

    @Test
    fun buyThenSellTest_resultSuccess() {
        exchangeClient.addCompany("company", 10, 10.0)
        userAccountClient.registerUser("user")
        userAccountClient.deposit(0, 1000.0)

        userAccountClient.buy(0, "company", 10)
        val response = userAccountClient.sell(0, "company", 10)
        assertEquals("OK", response)
    }

    companion object {
        const val EXCHANGE_PORT = 8000
        const val USER_ACCOUNT_PORT = 8001
    }
}