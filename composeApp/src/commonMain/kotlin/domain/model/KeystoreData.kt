package domain.model

data class Location(val city: String, val state: String, val countryCode: String)

object KeystoreData {
    val names = listOf(
        "Main", "Core", "App", "Client", "Server", "Web", "Mobile", "Desktop", "Admin", "User", "Gateway", "Service"
    )
    val organizations = listOf(
        "Innovate Inc", "Solutions LLC", "Tech Corp", "Global Co", "NextGen", "Quantum Leap", "Stellar", "Odyssey", "Apex", "Pioneer"
    )

    val locations = listOf(
        Location("San Francisco", "California", "US"),
        Location("New York", "New York", "US"),
        Location("London", "London", "GB"),
        Location("Manchester", "Greater Manchester", "GB"),
        Location("Tokyo", "Tokyo", "JP"),
        Location("Paris", "Ile-de-France", "FR"),
        Location("Sydney", "New South Wales", "AU"),
        Location("Berlin", "Berlin", "DE"),
        Location("Toronto", "Ontario", "CA"),
        Location("Mumbai", "Maharashtra", "IN"),
        Location("Shanghai", "Shanghai", "CN")
    )
}