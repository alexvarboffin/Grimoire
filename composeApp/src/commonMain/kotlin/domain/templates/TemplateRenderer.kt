package domain.templates

expect fun renderTemplate(templateContent: String, variables: Map<String, Any>): String
