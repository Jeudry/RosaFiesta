package com.adventist.adventist.api.controllers

import com.adventist.adventist.api.dto.ChatParticipantDto
import com.adventist.adventist.api.dto.ConfirmProfilePictureRequest
import com.adventist.adventist.api.dto.ProfilePictureUploadResponse
import com.adventist.adventist.api.mappers.toDto
import com.adventist.adventist.api.utils.requestUserId
import com.adventist.adventist.services.ChatParticipantService
import com.adventist.adventist.services.ProfilePictureService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/participants")
class ChatParticipantController (
  private val chatParticipantService: ChatParticipantService,
  private val profilePictureService: ProfilePictureService
){
  @GetMapping
    fun getChatParticipantsByUsernameOrEmail(
        @RequestParam(required = false) query: String
    ): ChatParticipantDto {
        val participant = if(query == null){
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }

        return participant?.toDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
  
  @PostMapping("/profile-picture-upload")
  fun getProfilePictureUploadUrl(
    @RequestParam mimeType: String
  ): ProfilePictureUploadResponse {
    return profilePictureService.generateUploadCredentials(
      userId = requestUserId,
      mimeType = mimeType
    ).toDto()
  }
  
  @PostMapping("/confirm-profile-picture")
  fun confirmProfilePicture(
    @Valid @RequestBody body: ConfirmProfilePictureRequest 
  ) {
    return profilePictureService.confirmProfilePictureUpload(
      userId = requestUserId,
      url = body.publicUrl
    )
  }
  
  @DeleteMapping("/profile-picture")
  fun deleteProfilePicture() {
    return profilePictureService.deleteProfilePicture(
      userId = requestUserId
    )
  }
}