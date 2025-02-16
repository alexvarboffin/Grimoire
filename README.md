:composeApp:run
gradle nativeBinaries
gradlew :composeApp:packageDistributionForCurrentOS
fun compareJSON(json1: String, json2: String): String {
return try {
val comparison = JSONCompare.compareJSON(json1, json2, JSONCompareMode.STRICT)
if (comparison.passed()) "✅ Same JSON" else "❗ Differences :\n ${comparison.message}"
} catch (e: JSONException) {
"❌ Format Error"
}
}