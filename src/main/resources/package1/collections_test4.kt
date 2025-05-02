package package1

fun testMutableCollections() {
    // These mutable collections can be replaced with immutable ones

    // ISSUE|Unnecessary Mutable Collection|This mutable collection can be replaced with List|7:17-7:29
    val list1 = customList()
    // ISSUE|Unnecessary Mutable Collection|This mutable collection can be replaced with List|9:17-9:22
    val list4 = xyz()
    // ISSUE|Unnecessary Mutable Collection|This mutable collection can be replaced with Set|11:16-11:43
    val set1 = mutableSetOf("a", "b", "c")

    println(list1[0])
    println(set1.contains("a"))

    // These mutable collections cannot be replaced with immutable ones

    val list2: MutableList<Int> = mutableListOf(4, 5, 6)
    val set2: MutableSet<String> = mutableSetOf("d", "e", "f")

    // Mutating operations
    list2.add(7)
    set2.remove("d")

    val list3: MutableList<Int> = mutableListOf(4, 5, 6) // This list is mutated, no issue
    mutate(list3)

    // Immutable collections
    val list7 = listOf(4, 5, 6)
    val set7 = setOf("d", "e", "f")
    val list8 = zyx()

}

fun mutate(list: MutableList<Int>) {
    // This function mutates the list
    list.add(1)
}

fun customList() = mutableListOf(1, 2, 3)
fun custom() = mutableListOf(1, 2, 3)
fun xyz() = mutableListOf(1, 2, 3)
fun zyx() = listOf(1, 2, 3)
