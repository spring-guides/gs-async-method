package com.example.asyncmethod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val name: String? = null,
    val blog: String? = null
)