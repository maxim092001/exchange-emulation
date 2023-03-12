package dao

import domain.Company

interface StocksDao {
    fun addCompany(company: Company)

    fun getCompanyByName(name: String): Company?

    fun getAllCompanies(): List<Company>

    fun updateCompany(company: Company)

    fun clear()
}