package com.example.sqllite

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@SpringBootApplication
class SqlLiteApplication

fun main(args: Array<String>) {
    runApplication<SqlLiteApplication>(*args)
}

@Component
class StartupService(
    repository: PersonRepository
) {
    init {
        println(
            repository.findAll()
        )
    }
}

@RestController
class PersonController(
    val repository: PersonRepository
) {
    @GetMapping
    fun getAll(): List<Person> = repository.findAll()

    @PostMapping
    fun create(@RequestBody person: Person) = repository.save(person)

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
