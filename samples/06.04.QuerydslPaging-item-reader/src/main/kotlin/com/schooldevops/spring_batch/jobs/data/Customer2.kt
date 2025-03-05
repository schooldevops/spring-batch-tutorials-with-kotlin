package com.schooldevops.spring_batch.jobs.data

import jakarta.persistence.*;

@Entity
@Table(name = "customer")
class Customer2 {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null
    lateinit var name: String
    var age: Int = 0
    lateinit var gender: String
}