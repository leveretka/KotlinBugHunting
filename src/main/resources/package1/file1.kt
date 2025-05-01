package package1

import java.util.Arrays

// ISSUE|Custom|Wrong return type|6:10-6:15
fun a(): Short = Short(1)
// ISSUE|Custom|Wrong return type|8:5-8:6
fun b() = Short(2)
fun c() = "Hello World"
fun d(): String = "Hello World"
