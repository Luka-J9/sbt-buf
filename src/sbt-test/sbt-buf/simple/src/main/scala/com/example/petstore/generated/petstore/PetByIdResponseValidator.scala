package com.example.petstore.generated.petstore

object PetByIdResponseValidator extends scalapb.validate.Validator[com.example.petstore.generated.petstore.PetByIdResponse] {
  def validate(input: com.example.petstore.generated.petstore.PetByIdResponse): scalapb.validate.Result =
    scalapb.validate.Result.optional(input.pet) { _value =>
      com.example.petstore.generated.petstore.PetValidator.validate(_value)
    }&&
    scalapb.validate.RequiredValidation("PetByIdResponse.pet", input.pet)
  
}