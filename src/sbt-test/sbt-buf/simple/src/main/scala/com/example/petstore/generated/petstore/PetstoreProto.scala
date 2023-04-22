// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.example.petstore.generated.petstore

object PetstoreProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    io.envoyproxy.pgv.validate.validate.ValidateProto,
    scalapb.options.ScalapbProto,
    scalapb.validate.validate.ValidateProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      com.example.petstore.generated.petstore.Pet,
      com.example.petstore.generated.petstore.User,
      com.example.petstore.generated.petstore.PetByIdRequest,
      com.example.petstore.generated.petstore.UserByNameRequest,
      com.example.petstore.generated.petstore.PetByIdResponse,
      com.example.petstore.generated.petstore.UserByNameResponse,
      com.example.petstore.generated.petstore.ListUsersRequest,
      com.example.petstore.generated.petstore.StoreUsersResponse
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CixzcmMvbWFpbi9wcm90b2J1Zi9wZXRzdG9yZS92MS9wZXRzdG9yZS5wcm90bxILcGV0c3RvcmUudjEaF3ZhbGlkYXRlL3Zhb
  GlkYXRlLnByb3RvGhVzY2FsYXBiL3NjYWxhcGIucHJvdG8aH3NjYWxhcGIvdmFsaWRhdGUvdmFsaWRhdGUucHJvdG8iPQoDUGV0E
  hcKAmlkGAEgASgFQgfiPwQSAmlkUgJpZBIdCgRuYW1lGAIgASgJQgniPwYSBG5hbWVSBG5hbWUijgEKBFVzZXISFwoCaWQYASABK
  AVCB+I/BBICaWRSAmlkEikKCHVzZXJuYW1lGAIgASgJQg3iPwoSCHVzZXJuYW1lUgh1c2VybmFtZRIgCgVlbWFpbBgDIAEoCUIK4
  j8HEgVlbWFpbFIFZW1haWwSIAoFcGhvbmUYBCABKAlCCuI/BxIFcGhvbmVSBXBob25lIikKDlBldEJ5SWRSZXF1ZXN0EhcKAmlkG
  AEgASgFQgfiPwQSAmlkUgJpZCJFChFVc2VyQnlOYW1lUmVxdWVzdBIwCgh1c2VybmFtZRgBIAEoCUIU4j8KEgh1c2VybmFtZfpCB
  HICEANSCHVzZXJuYW1lIkcKD1BldEJ5SWRSZXNwb25zZRI0CgNwZXQYASABKAsyEC5wZXRzdG9yZS52MS5QZXRCEOI/BRIDcGV0+
  kIFigECEAFSA3BldCJGChJVc2VyQnlOYW1lUmVzcG9uc2USMAoEdXNlchgBIAEoCzIRLnBldHN0b3JlLnYxLlVzZXJCCeI/BhIEd
  XNlclIEdXNlciIuChBMaXN0VXNlcnNSZXF1ZXN0EhoKA21zZxgBIAEoCUII4j8FEgNtc2dSA21zZyIwChJTdG9yZVVzZXJzUmVzc
  G9uc2USGgoDbXNnGAEgASgJQgjiPwUSA21zZ1IDbXNnMuICCg9QZXRTdG9yZVNlcnZpY2USRAoHUGV0QnlJZBIbLnBldHN0b3JlL
  nYxLlBldEJ5SWRSZXF1ZXN0GhwucGV0c3RvcmUudjEuUGV0QnlJZFJlc3BvbnNlEk0KClVzZXJCeU5hbWUSHi5wZXRzdG9yZS52M
  S5Vc2VyQnlOYW1lUmVxdWVzdBofLnBldHN0b3JlLnYxLlVzZXJCeU5hbWVSZXNwb25zZRI/CglMaXN0VXNlcnMSHS5wZXRzdG9yZ
  S52MS5MaXN0VXNlcnNSZXF1ZXN0GhEucGV0c3RvcmUudjEuVXNlcjABEkIKClN0b3JlVXNlcnMSES5wZXRzdG9yZS52MS5Vc2VyG
  h8ucGV0c3RvcmUudjEuU3RvcmVVc2Vyc1Jlc3BvbnNlKAESNQoJQnVsa1VzZXJzEhEucGV0c3RvcmUudjEuVXNlchoRLnBldHN0b
  3JlLnYxLlVzZXIoATABQi4KHmNvbS5leGFtcGxlLnBldHN0b3JlLmdlbmVyYXRlZOI/C1gAikQGCAEQASgAYgZwcm90bzM="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      io.envoyproxy.pgv.validate.validate.ValidateProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor,
      scalapb.validate.validate.ValidateProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}