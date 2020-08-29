package com.example.threadingnetwork

class President(var name: String, var startDuty: Int, var endDuty: Int, var description: String) : Comparable<President> {

    override fun compareTo(other: President): Int {
        return this.startDuty.compareTo(other.startDuty)
    }

    override fun toString(): String {
        return "$name $startDuty - $endDuty"
    }
}

object GlobalModel {
    val presidents: MutableList<President> = java.util.ArrayList()

    init {
        presidents.add(President("Kaarlo Stahlberg", 1919, 1925, "Eka pressa"))
        presidents.add(President("Lauri Relander", 1925, 1931, "Toka pressa"))
        presidents.add(President("P. E. Svinhufvud", 1931, 1937, "Kolmas pressa"))
        presidents.add(President("Kyösti Kallio", 1937, 1944, "Neljäs pressa"))
        presidents.add(President("Risto Ryti", 1940, 1944, "Viides pressa"))
        presidents.add(President("Carl Gustaf Emil Mannerheim", 1944, 1946, "Kuudes pressa"))
        presidents.add(President("Juho Kusti Paasikivi", 1946, 1956, "Äkäinen ukko"))
        presidents.add(President("Urho Kekkonen", 1956, 1982, "Pelimies"))
        presidents.add(President("Mauno Koivisto", 1982, 1994, "Manu"))
        presidents.add(President("Martti Ahtisaari", 1994, 2000, "Mahtisaari"))
        presidents.add(President("Tarja Halonen", 2000, 2012, "Eka naispressa"))
        presidents.sort()
    }

}