# sbt-buf

An SBT AutoPlugin that lightly wraps the Buf CLI tool.

## Setup
This plugin requires sbt 1.0.0+

Install this plugin by including the following in your `project/plugins.sbt`

```scala
addSbtPlugin("com.lukaj9" % "sbt-buf" % "<VERSION>")
```

It's recommended to familiarize yourself with the [Buf Cli Tool](https://buf.build/docs/ecosystem/cli-overview) as setup and use are well documented, and this project is only intended to make Scala integration easier.

## What is Buf and why would I use it?
To pull directly from Buf project's [README](https://github.com/bufbuild/buf/blob/main/README.md)


The `buf` CLI is a tool for working with Protocol Buffers.

- The ability to manage Protobuf assets on the Buf Schema Registry (BSR).
- A linter that enforces good API design choices and structure.
- A breaking change detector that enforces compatibility at the source code or wire level.
- A generator that invokes your plugins based on configurable templates.
- A formatter that formats your Protobuf files in accordance with industry standards.
- Integration with the Buf Schema Registry, including full dependency management.


If this sounds interesting, and you want to delve further into all the things Buf can do for you, I'd recommend taking a look at [Buf's Documentation](https://buf.build/docs/introduction)


## Why this plugin?
This plugin was built to serve two main purposes:
- Making it easier for teams to collaborate on a project
- Integrate Buf into the build lifecycle
- No reliance on published Jars

This plugin simplifies collaboration by Downloading the Buf release of your choice in the scope of your project. This ensures that all teams are using the same version of Buf without polluting your computer with different installations.

Similar to installation of Buf, this plugin will also manage your protoc plugins (these can be run natively by Buf), also in the scope of your project. Typically a user will have to install each protoc plugin the application uses on their computers path and ensure that if different projects have different versions they are appropriately named. This is tedious and error prone.

While Buf has a native solution for this called [Remote Plugins](https://buf.build/plugins), these plugins are managed by the Buf team themselves, so it may not include your desired plugin. At the time of writing only two of the many Scala plugins are available for consumption. This leads to a catch 22 where support for Scala plugins are low because demand is low because integration is difficult. Hopefully this project fixes that!

If you have a plugin you wish to see supported create or bump an issue in [buf plugins](https://github.com/bufbuild/plugins/issues) project. In the meantime hopefully the protoc plugins will bridge that gap.

Using Buf (even without BSR), you are no longer tied to protobuf files needing to be published in Maven in order to be accessable, all code is generated locally for your project.

## Usage

See example project [here](https://github.com/Luka-J9/sbt-buf-example/)

### Applying the plugin in your build.sbt
```scala
enablePlugins(BufPlugin)
```

This will give you an "out of the box" setup, if you wish to customize your behavior the following setting are available.

### `bufVersion` setting
This setting pins your Buf CLI version to the one selected when downloading from the github releases.
The default is None, which will Download the latest.

*Example*: `Some("vX.Y.Z")` 

*Default*: `None`

*Note*: The version can be found on the [Buf releases page](https://github.com/bufbuild/buf/releases). Additionally this will only compute the latest the first time you download the CLI tool, you'll have to run a clean if you want to refetch the latest.

### `detectedSystem` setting
This setting records what the detected system, this determines what version of Buf is installed, and can be used to in other settings to determine what other os and architecture specific files to download. 

*Example*: `com.lukaj9.sbtbuf.platform.DetectedSystem(os = Osx, arch = X86_64)`

*Default*: Your system's OS and Arch, if it can be determined.

*Note*: that at the time of writing only Linux/Darwin/Windows and x86_64 and ARM are supported. If this changes a please raise an issue - otherwise you'll likely have to use the next setting.

### `bufOverride` setting
This setting allows you to choose a different location to pull your Buf CLI tool from. This is for use cases where downloading from Github releases is not viable.

*Example*: `Some(new java.net.URI("https://selfhosted.com/buf"))`

*Default*: `None``

*Note*: Buf is OS and architecture dependent, so you'll need all variants and pattern match on the system type if you want to support multiple OS and architecture types

### `protocPlugins` setting
This setting allows you to define what protoc plugins should be downloaded and what they should be named. 

*Example*:
```scala
lazy val validatePlugin = "https://repo1.maven.org/maven2/com/thesamet/scalapb/protoc-gen-scalapb-validate/0.3.4/protoc-gen-scalapb-validate-0.3.4"
protocPlugins := Seq(
    ProtocPlugin(
        "protoc-gen-scalapb-validate",
        detectedSystem.value.os match {
            case Windows => new URI(s"$validatePlugin-windows.bat")
            case Linux | Osx => new URI(s"$validatePlugin-unix.sh")
            case e => throw new IllegalArgumentException(s"$e is not a supported os for this plugin")
        }
    )
)
```

*Default*: `Seq.empty`

*Note*: Like Buf protoc plugins are typically OS dependent, so you'll likely need to pattern match. Additionally it's recommended that the naming convention adhears to the standard `protoc-gen-<plugin-name>` as Buf expects that naming convention.

### `bufPath` setting
This setting allows you to define where your `buf.yaml` or `buf.work.yaml` file is.

*Example*: `baseDirectory.value.toPath().resolve("src/main/protobuf")`

*Default*: `baseDirectory.value.toPath()`

*Note*: It is highly recommended that this is set relative to your `baseDirectory`, this is due to the fact that Buf functions off of relative paths and recursively finds protofiles when doing things like code generation. This will likely be more strictly enforced in future versions of the plugin. Additionally it is worth noting that while buf doesn't take issue with having a buf file and a workspace file in the same directory, this plugin will explicitly error out in these cases. This due to some book keeping needed for code generation, as well as keeping the configurations for this plugin simple. 

### `bufBreakingCommand` setting
This setting allows you to define the configuration of how to evaluate breaking changes. Since this is dependent on the user's preference the default cases is None.
See [Buf Breaking Against](https://buf.build/docs/reference/cli/buf/breaking#against) for usage

### `buf` task
This is the escape hatch - if there are ease of use functionality missing with this plugin you can leverage this to execute any arbitrary buf command in SBT. Arguments after `buf` are passed to the CLI tool as is. You really shouldn't need to rely on this, so if you are finding yourself needing to use this please raise an issue with your use case!

Tip: If your proto files don't exist in BSR yet - take a look at the CLI doc for buf generate, you can do fancy things like generate protos from a remote git project. This will likely be added as a feature to configure in the plugin in the future.

### `bufGenerate` task
Generates code according to the `buf.gen.yaml` file. This file should exist in the same directory as the `bufPath` setting. This setting creates a hash of the buf files used, and the proto files processed, so this can be tied into your compile phase little overhead.

### `generateBufDependencies` task
Looks at the dependencies found `buf.yaml` file and generates them individually.

Tip: Unlike the Buf CLI tool you can use this command and the `buf.yaml` file to generate only dependencies with no additional local proto files. This is useful if you have various clients defined in BSR and want to generate the code locally.

### `generateBufWithImports` task
Looks at the dependency list found in `buf.yaml` file and generates them individually. This will also generate the various imports of those dependencies. If there are transitive dependencies it's recommended that you use the option without imports.

Tip: See tip for `generateBufDependencies`


### `bufDownload` task
Downloads the Buf Cli Tool given the settings above. You really shouldn't need to worry about this comm

### `bufDownloadGen` task
Downloads the Protoc Plugins used for code generation if they exist

### `bufLint` task
Runs Buf Lint of the directory - return 100 if the lint is unsucceful

### `bufBreaking` task
Runs Buf Breaking for the directory using the values configured in the `bufBreakingCommand` - will return 100 if the changes are deemed incompatible


### Testing

Run `test` for regular unit tests. A set of these tests pull resources from buf's github release page. 
So you'll need internet access. If Github throws a 403 for these requests give it a moment and try again

### CI

The generated project uses [sbt-github-actions](https://github.com/djspiewak/sbt-github-actions) as a plugin to generate workflows for GitHub actions. For full details of how to use it [read this](https://github.com/djspiewak/sbt-github-actions/blob/main/README.md)

### Publishing

1. publish your source to GitHub
2. Follow the instructions in [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release/blob/main/readme.md) to create a sonatype account and setup your keys
3. `sbt ci-release`
4. [Add your plugin to the community plugins list](https://github.com/sbt/website#attention-plugin-authors)
5. [Claim your project an Scaladex](https://github.com/scalacenter/scaladex-contrib#claim-your-project)
