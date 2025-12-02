package com.adventist.adventist.infra.storage

import com.adventist.adventist.domain.exceptions.InvalidProfilePictureEx
import com.adventist.adventist.domain.exceptions.StorageEx
import com.adventist.adventist.domain.models.ProfilePictureUploadCredentials
import com.adventist.adventist.domain.types.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.*

@Service
class SupabaseStorageService(
  @param:Value("\${supabase.url}") private val supabaseUrl: String,
  private val supabaseRestClient: RestClient,
) {
  companion object {
    private val allowedMimeTypes = mapOf(
      "image/jpeg" to "jpg",
      "image/jpg" to "jpg",
      "image/png" to "png",
      "image/webp" to "webp",
    )
  }
  
  fun generateSignedUploadUrl(userId: UserId, mimeType: String): ProfilePictureUploadCredentials {
    val extension = allowedMimeTypes[mimeType]
      ?: throw InvalidProfilePictureEx("Invalid mime type $mimeType")
    
    val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
    val path = "profile-pictures/$fileName"
    
    val uploadUrl = "$supabaseUrl/storage/v1/object/public/$path"
    val publicUrl = uploadUrl
    
    return ProfilePictureUploadCredentials(
      uploadUrl = createSignedUrl(path, 300),
      publicUrl = publicUrl,
      headers = mapOf(
        "Content-Type" to mimeType
      ),
      expiresAt = Instant.now().plusSeconds(300)
    )
  }
  
  fun deleteFile(url: String) {
    val path = if(url.contains("/object/public/")) {
      url.substringAfter("/object/public/")
    } else throw StorageEx("Invalid file URL format")
    
    val deleteUrl = "/storage/v1/object/$path"
    
    val response = supabaseRestClient
      .delete()
      .uri(deleteUrl)
      .retrieve()
      .toBodilessEntity()
    
    if(response.statusCode.isError) {
      throw StorageEx("Unable to delete file: ${response.statusCode.value()}")
    }
  }
  
  private fun createSignedUrl(
    path: String,
    expiresInSeconds: Int
  ): String {
    val json = """
            { "expiresIn": $expiresInSeconds }
        """.trimIndent()
    
    val response = supabaseRestClient
      .post()
      .uri("/storage/v1/object/upload/sign/$path")
      .header("Content-Type", "application/json")
      .body(json)
      .retrieve()
      .body(SignedUploadResponse::class.java)
      ?: throw StorageEx("Failed to create signed URL")
    
    return "$supabaseUrl/storage/v1${response.url}"
  }
  
  private data class SignedUploadResponse(
    val url: String
  )
}