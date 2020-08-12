package com.example.basics

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication
class BasicsApplication

fun main(args: Array<String>) {
  runApplication<BasicsApplication>(*args)
}

@Component
class Runner(private val customerService: CustomerService)
  : ApplicationListener<ApplicationReadyEvent> {

  override fun onApplicationEvent(are: ApplicationReadyEvent) {
    customerService
        .all()
        .forEach {
          val record = this.customerService.byId(it.id)!!
          println("the customer id ${record.id} and the name is ${record.name}")
        }
  }
}


object Customers : Table() {

  val id = integer("id").autoIncrement()
  val name = varchar("name", 255)

  override val primaryKey: PrimaryKey = PrimaryKey(this.id)

}

@Service
@Transactional
class ExposedCustomerService : CustomerService {

  override fun all(): Collection<Customer> =
      Customers
          .selectAll()
          .map { Customer(it[Customers.id], it[Customers.name]) }

  override fun byId(id: Int): Customer? =
      Customers
          .select { Customers.id.eq(id) }
          .map { Customer(it[Customers.id], it[Customers.name]) }
          .firstOrNull()

}

//@Service
class JdbcCustomerService(private val jdbcTemplate: JdbcTemplate) : CustomerService {

  override fun all(): Collection<Customer> =
      this.jdbcTemplate.query("select * from CUSTOMERS") { rs, _ ->
        Customer(rs.getInt("id"), rs.getString("name"))
      }

  override fun byId(id: Int): Customer? =
      this.jdbcTemplate.queryForObject("select * from CUSTOMERS where id =? ", id) { resultSet, i ->
        Customer(resultSet.getInt("id"), resultSet.getString("name"))
      }

}

data class Customer(val id: Int, val name: String)

interface CustomerService {

  fun all(): Collection<Customer>

  fun byId(id: Int): Customer?
}