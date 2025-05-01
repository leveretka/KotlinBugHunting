package package1

fun testMutableCollections() {
    // These mutable collections can be replaced with immutable ones

    // ISSUE|Unnecessary Mutable Collection|This mutable collection can be replaced with List|7:16-7:32
    val list1: MutableList<Int> = mutableListOf(1, 2, 3)
    // ISSUE|Unnecessary Mutable Collection|This mutable collection can be replaced with Set|9:15-9:33
    val set1: MutableSet<String> = mutableSetOf("a", "b", "c")

    println(list1[0])
    println(set1.contains("a"))

    // These mutable collections cannot be replaced with immutable ones

    val list2: MutableList<Int> = mutableListOf(4, 5, 6)
    val set2: MutableSet<String> = mutableSetOf("d", "e", "f")

    // Mutating operations
    list2.add(7)
    set2.remove("d")
}
