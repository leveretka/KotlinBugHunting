package package2

import package1.D
import package1.My
import kotlin.reflect.KProperty

class MyClass {
    companion object {
        const val MY_CONSTANT = 42
    }

}

class MyClass2 {
    companion object Named {
        const val MY_CONSTANT = 100500
    }
}


operator fun D.getValue(my: My, property: KProperty<*>): Any {
    TODO("Not yet implemented")
}

