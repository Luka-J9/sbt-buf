package com.example.petstore.generated.petstore

object UserByNameResponseValidator extends scalapb.validate.Validator[com.example.petstore.generated.petstore.UserByNameResponse] {
  def validate(input: com.example.petstore.generated.petstore.UserByNameResponse): scalapb.validate.Result =
    scalapb.validate.Result.optional(input.user) { _value =>
      com.example.petstore.generated.petstore.UserValidator.validate(_value)
    }
  
}