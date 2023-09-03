package com.kemp.model

class GenericException(message: String, val statusCode: Int, val errorDetail: String) : Exception(message) {
}
