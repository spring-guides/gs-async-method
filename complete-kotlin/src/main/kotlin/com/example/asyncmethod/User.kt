package com.example.asyncmethod

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(var name: String? = null, var blog: String? = null)
