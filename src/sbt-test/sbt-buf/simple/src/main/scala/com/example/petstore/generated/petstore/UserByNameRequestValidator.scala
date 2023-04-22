package com.example.petstore.generated.petstore

object UserByNameRequestValidator extends scalapb.validate.Validator[com.example.petstore.generated.petstore.UserByNameRequest] {
  def validate(input: com.example.petstore.generated.petstore.UserByNameRequest): scalapb.validate.Result =
    scalapb.validate.Result.run(io.envoyproxy.pgv.StringValidation.minLength("UserByNameRequest.username", input.username, 3))
  
}