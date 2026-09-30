package com.example.asyncmethod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(val name: String?, val blog: String?)
