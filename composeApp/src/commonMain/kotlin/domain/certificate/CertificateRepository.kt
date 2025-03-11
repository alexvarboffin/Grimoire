package domain.certificate

interface CertificateRepository {
    suspend fun grabCertificates(hostname: String, outputPath: String): List<CertificateInfo>
}

data class CertificateInfo(
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validUntil: String,
    val pinHash: String
) 