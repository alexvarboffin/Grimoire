package domain.templates

expect fun renderTemplate(templateContent: String, variables: Map<String, String>): String
