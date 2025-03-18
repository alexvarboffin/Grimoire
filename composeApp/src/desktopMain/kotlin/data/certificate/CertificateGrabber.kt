// ... existing code ...
package data.certificate

import domain.certificate.CertificateInfo
import domain.certificate.CertificateRepository
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

actual class CertificateGrabber : CertificateRepository {
    override suspend fun grabCertificates(
        hostname: String,
        outputPath: String
    ): List<CertificateInfo> {
        val certificates = mutableListOf<CertificateInfo>()
        
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                chain.forEach { cert ->
                    val md = MessageDigest.getInstance("SHA-256")
                    val digest = md.digest(cert.publicKey.encoded)
                    val base64Hash = Base64.getEncoder().encodeToString(digest)
                    val pin = "sha256/$base64Hash"

                    certificates.add(
                        CertificateInfo(
                            subject = cert.subjectDN.toString(),
                            issuer = cert.issuerDN.toString(),
                            validFrom = cert.notBefore.toString(),
                            validUntil = cert.notAfter.toString(),
                            pinHash = pin
                        )
                    )
                }

                // Сохраняем в файл
                File(outputPath).writeText(buildString {
                    appendLine("Hostname: $hostname")
                    certificates.forEachIndexed { index, cert ->
                        appendLine("Certificate ${index + 1}:")
                        appendLine("Subject: ${cert.subject}")
                        appendLine("Issuer: ${cert.issuer}")
                        appendLine("Valid from: ${cert.validFrom}")
                        appendLine("Valid until: ${cert.validUntil}")
                        appendLine("Pin hash: ${cert.pinHash}")
                        appendLine()
                    }
                })
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)

        val url = URL("https://$hostname")
        val conn = url.openConnection() as HttpsURLConnection
        conn.sslSocketFactory = sslContext.socketFactory

        try {
            conn.connect()
        } catch (e: Exception) {
            // Игнорируем - хэши уже получены
        } finally {
            conn.disconnect()
        }

        return certificates
    }
} 