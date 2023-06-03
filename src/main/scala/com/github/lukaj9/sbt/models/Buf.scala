package com.github.lukaj9.sbt.models

import java.nio.file.Path
import scala.util._
import javax.xml.stream.events.Comment

final case class Buf(
    version: Version,
    name: Option[Name],
    deps: List[Dependency],
    build: Option[Build],
    breaking: Option[BreakingCategory],
    lint: Option[Lint],
)

case class Lint(
    use: List[LintCategory], 
    except: List[LintCategory], 
    ignore: List[RelativePath], 
    ignoreOnly: Map[LintCategory, List[RelativePath]], 
    ignoreUnstablePackages: Boolean,
    allowCommentIgnores: Boolean,
    enumZeroValueSuffix: Option[String],
    rpcAllowSameRequestResponse: Boolean,
    rpcAlloowGoogleProtobufEmptyRequests: Boolean,
    serviceSuffix: Option[String])

case class Breaking(
    use: List[BreakingCategory], 
    except: List[BreakingCategory], 
    ignore: List[RelativePath], 
    ignoreOnly: Map[BreakingCategory, List[RelativePath]], 
    ignoreUnstablePackages: Boolean)


case class RelativePath private(path: Path) extends AnyVal
object RelativePath {
    def apply(path: Path): Try[RelativePath] = {
        if(path.isAbsolute() || path.normalize().startsWith("..")) 
            Failure(new IllegalArgumentException(s"Path `${path.toString}` must be relative"))
        else Success(new RelativePath(path))
    }
}

sealed trait LintCategory 

object LintCategory {
    sealed trait MinimalLintCategory extends LintCategory
    sealed trait BasicLintCategory extends LintCategory
    sealed trait DefaultLintCategory extends LintCategory
    sealed trait CommentLintCategory extends LintCategory
    sealed trait UnaryRuleLintCategory extends LintCategory

    case class UnknownLintCategory(value: String) extends LintCategory

    case object COMMENTS extends CommentLintCategory
    case object COMMNET_ENUM extends CommentLintCategory
    case object COMMENT_ENUM_VALUE extends CommentLintCategory
    case object COMMENT_FIELD extends CommentLintCategory
    case object COMMENT_MESSAGE extends CommentLintCategory
    case object COMMENT_ONEOF extends CommentLintCategory
    case object COMMENT_RPC extends CommentLintCategory
    case object COMMENT_SERVICE extends CommentLintCategory

    case object UNARY_RPC extends UnaryRuleLintCategory
    case object RPC_NO_CLIENT_STREAMING extends UnaryRuleLintCategory
    case object RPC_NO_SERVER_STREAMING extends UnaryRuleLintCategory

    case object PACKAGE_NO_IMPORT_CYCLE extends LintCategory

    case object DEFAULT extends DefaultLintCategory with BasicLintCategory with MinimalLintCategory

    case object BASIC extends BasicLintCategory
    case object ENUM_PASCAL_CASE extends BasicLintCategory
    case object ENUM_VALUE_UPPER_SNAKE_CASE extends BasicLintCategory
    case object FIELD_LOWER_SNAKE_CASE extends BasicLintCategory
    case object MESSAGE_PACSAL_CASE extends BasicLintCategory
    case object ONEOF_LOWER_SNAKE_CASE extends BasicLintCategory
    case object RPC_PASCAL_CASE extends BasicLintCategory
    case object SERVICE_PASCAL_CASE extends BasicLintCategory
    case object PACKAGE_SAME_CSHARP_NAMESPACE extends BasicLintCategory
    case object PACKAGE_SAME_GO_PACKAGE extends BasicLintCategory
    case object PACKAGE_SAME_JAVA_MULTIPLE_FILES extends BasicLintCategory
    case object PACKAGE_SAME_JAVA_PACKAGE extends BasicLintCategory
    case object PACKAGE_SAME_PHP_NAMESPACE extends BasicLintCategory
    case object PACKAGE_SAME_RUBY_PACKAGE extends BasicLintCategory
    case object PACKAGE_SAME_SWIFT_PREFIX extends BasicLintCategory
    case object ENUM_FIRST_VALUE_ZERO extends BasicLintCategory
    case object ENUM_NO_ALLOW_ALIAS extends BasicLintCategory
    case object IMPORT_NO_WEAK extends BasicLintCategory
    case object IMPORT_NO_PUBLIC extends BasicLintCategory
    case object IMPORT_USED extends BasicLintCategory

    case object MINIMAL extends MinimalLintCategory with BasicLintCategory
    case object DIRECTORY_SAME_PACKAGE extends MinimalLintCategory
    case object PACKAGE_DEFINED extends MinimalLintCategory
    case object PACKAGE_SAME_DIRECTORY extends MinimalLintCategory
    case object PACKAGE_DIRECTORY_MATCH extends MinimalLintCategory 
}

sealed trait BreakingCategory

object BreakingCategory {
    case object Default extends BreakingCategory
    case object File extends BreakingCategory
    case object Package extends BreakingCategory
    case object WireJson extends BreakingCategory
    case object Wire extends BreakingCategory
    case object EnumNoDelete extends BreakingCategory
    case object MessageNoDelete extends BreakingCategory
    case object ServiceNoDelete extends BreakingCategory
    case object PackageEnumNoDelete extends BreakingCategory
    case object PackageMessageNoDelete extends BreakingCategory
    case object PackageServiceNoDelete extends BreakingCategory
    case object FileNoDelete extends BreakingCategory
    case object PackageNoDelete extends BreakingCategory
    case object EnumValueNoDelete extends BreakingCategory
    case object FieldNoDelete extends BreakingCategory
    case object EnumValueNoDeleteUnlessNameReserved extends BreakingCategory
    case object FieldNoDeleteUnlessNumberReserved extends BreakingCategory
    case object RpcNoDelete extends BreakingCategory
    case object OneOfNoDelete extends BreakingCategory
    case object FileSameSyntax extends BreakingCategory
    case object FileSameCCEnableArenas extends BreakingCategory
    case object FileSameCCGenericServices extends BreakingCategory
    case object FileSameCSharpNamespace extends BreakingCategory
    case object FileSameGoPackage extends BreakingCategory
    case object FileSameJavaGenericServices extends BreakingCategory
    case object FileSameJavaMultipleFiles extends BreakingCategory
    case object FileSameJavaOuterClassname extends BreakingCategory
    case object FileSameJavaPackage extends BreakingCategory
    case object FileSameJavaStringCheckUtf8 extends BreakingCategory
    case object FileSameObjcClassPrefix extends BreakingCategory
    case object FileSameOptimizeFor extends BreakingCategory
    case object FileSamePhpClassPrefix extends BreakingCategory
    case object FileSamePhpGenericServices extends BreakingCategory
    case object FileSamePhpMetadataNamespace extends BreakingCategory
    case object FileSamePhpNamespace extends BreakingCategory
    case object FileSamePyGenericServices extends BreakingCategory
    case object FileSameRubyPackage extends BreakingCategory
    case object FileSameSwiftPrefix extends BreakingCategory
    case object EnumValueSameName extends BreakingCategory
    case object FieldSameCType extends BreakingCategory
    case object FieldSameJstype extends BreakingCategory
    case object FieldSameType extends BreakingCategory
    case object FiledWireComaptibleType extends BreakingCategory
    case object FieldWireJsonCompatibleType extends BreakingCategory
    case object FieldSameLabel extends BreakingCategory
    case object FieldSameOneof extends BreakingCategory
    case object FieldSameName extends BreakingCategory
    case object FieldSameJsonName extends BreakingCategory
    case object ReservedEnumNoDelete extends BreakingCategory
    case object ReservedMessageNoDelete extends BreakingCategory
    case object ExtensionMessageNoDelete extends BreakingCategory
    case object MessageSameMessageSetWireFormat extends BreakingCategory
    case object MessageNoRemoveStandardDescriptorAccessor extends BreakingCategory
    case object RPCSameRequestType extends BreakingCategory
    case object RPCSameResponseType extends BreakingCategory
    case object RPCSameClientStreaming extends BreakingCategory
    case object RPCSameServerStreaming extends BreakingCategory
    case object RPCSameIdempotencyLevel extends BreakingCategory
    case class UnknownBreaking(value: String) extends BreakingCategory
}

case class Build(excludes: List[Excludes])
case class Excludes(values: List[String])

case class Version(value: String) extends AnyVal
case class Name(value: String) extends AnyVal
case class Dependency(root: Root, org: Organization, project: Project)
case class Root(value: String) extends AnyVal
case class Organization(value: String) extends AnyVal
case class Project(value: String) extends AnyVal