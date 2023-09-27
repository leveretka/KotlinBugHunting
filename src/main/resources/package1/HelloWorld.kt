package package1

import package2.MyClass
import package2.MyClass2
import package2.getValue
import java.util.List

fun hello() {
    val x = 5 + 6
    println(x)
    println(MyClass.MY_CONSTANT)
    println(MyClass2.MY_CONSTANT)
}

class My (delegate: D) {
    val p by delegate
}

class D
