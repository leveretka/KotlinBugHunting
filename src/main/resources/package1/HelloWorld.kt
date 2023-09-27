package package1

import package2.MyClass
import package2.MyClass2
import package2.MyList
import package2.contains
import package2.invoke
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

class My2 (val list: MyList) {
    fun f () {
        1 in list
        2 !in list
        list(0)
    }
}


class D
