package domain.model

object KeystoreData {
    val names = listOf(
        "Main", "Core", "App", "Client", "Server", "Web", "Mobile", "Desktop", "Admin", "User"
    )
    val organizations = listOf(
        "Innovate Inc", "Solutions LLC", "Tech Corp", "Global Co", "NextGen", "Quantum Leap", "Stellar", "Odyssey"
    )
    val cities = listOf(
        "San Francisco", "New York", "London", "Tokyo", "Berlin", "Paris", "Sydney", "Singapore"
    )
    val states = listOf(
        "California", "New York", "London", "Tokyo", "Berlin", "Ile-de-France", "New South Wales", "Singapore"
    )
    val countries = mapOf(
        "US" to "United States",
        "GB" to "United Kingdom",
        "JP" to "Japan",
        "DE" to "Germany",
        "FR" to "France",
        "AU" to "Australia",
        "SG" to "Singapore",
        "CA" to "Canada",
        "IN" to "India",
        "CN" to "China"
    )
}
