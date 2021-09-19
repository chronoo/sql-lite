package com.example.sqllite

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.persistence.*

@SpringBootApplication
class SqlLiteApplication

fun main(args: Array<String>) {
    runApplication<SqlLiteApplication>(*args)

//    DriverManager.getConnection("jdbc:sqlite:sample.db")?.use {
//        it.createStatement().apply {
//            queryTimeout = 30
//            executeUpdate("create table person (id integer, name string)")
//            executeUpdate("insert into person values(1, 'leo')")
//        }
//    }
}

@Component
class StartupService(
    service: CustomPersonService
) {
    init {
        println(
            service.getAll()
        )
    }
}

@Service
class CustomPersonService(
    val sqlLiteConfig: DataSourceProperties
) {
    private lateinit var connection: Connection

    @PostConstruct
    fun initConnection() {
        println("create connection...")
        connection = DriverManager.getConnection(sqlLiteConfig.url)
    }

    @PreDestroy
    fun closeConnection() {
        println("close connection...")
        connection.close()
    }

    fun addPerson(id: Int?, name: String) =
        createStatement()?.executeUpdate("insert into person values($id, '$name')")

    fun addPerson(person: Person) = person.apply { addPerson(id, name) }

    fun getAll() =
        createStatement()?.executeQuery("select * from person")
            ?.collect(ResultSet::next) { Person(it.getString("name"), it.getInt("id")) }
            ?: emptyList()

    private fun createStatement() =
        connection.createStatement()?.apply {
            queryTimeout = 30
        }
}

@RestController
class PersonController(
    val repository: PersonRepository,
    val customService: CustomPersonService
) {
    @GetMapping
    fun getAll(): List<Person> = repository.findAll()

    @PostMapping
    fun create(@RequestBody person: Person) = customService.addPerson(person)

    @DeleteMapping
    fun clear() = repository.deleteAll()
}

@Entity
class Person(
    val name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
) {
    constructor() : this("")

    override fun toString(): String = "$id $name"
}

@Repository
interface PersonRepository : JpaRepository<Person, Int>

fun <T, R> T.collect(condition: (T) -> Boolean, map: (T) -> R): List<R> =
    mutableListOf<R>().also {
        while (condition(this)) {
            it += map(this)
        }
    }
