package dao

import domain.Company

class StocksInMemoryDao : StocksDao {
    private val mp: MutableMap<String, Company> = mutableMapOf()

    override fun addCompany(company: Company) {
        mp[company.name] = company
    }

    override fun getCompanyByName(name: String): Company? = mp[name]

    override fun getAllCompanies(): List<Company> = mp.values.toList()

    override fun updateCompany(company: Company) {
        mp[company.name] = company
    }

    override fun clear() = mp.clear()
}