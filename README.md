This is a demonstration of how to build a Static code analyzer for Kotlin based on the Standalone Analysis API.

Use `./gradlew build` to build the project

Use `./gradle run` to run the application

You can also adjust the rules used in the app by changing these lines in the `KtorApp.kt`:

```
val analyzer = Analyzer(listOf(
                CustomRule(),
                MutableCollectionRule2(),
                BadNameFunctionNameRule(),
            ))
```

Test examples use such comments convention to mark the issues: `// ISSUE|RuleName|RuleMessage|Location`

See example:
```
fun goodName() = "Hello World"
// ISSUE|BadNameFunctionName|This is a bad name|3:5-3:13
fun bad_name() = "Hello World"
```
