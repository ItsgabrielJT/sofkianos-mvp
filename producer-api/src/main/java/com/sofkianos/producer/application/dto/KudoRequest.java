package com.sofkianos.producer.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Kudo requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KudoRequest {

  @NotBlank(message = "The 'from' email is required")
  @Email(message = "The 'from' field must be a valid email address")
  private String from;

  @NotBlank(message = "The 'to' email is required")
  @Email(message = "The 'to' field must be a valid email address")
  private String to;

  @NotBlank(message = "The category is required")
  @Pattern(regexp = "^(Innovation|Teamwork|Passion|Mastery)$", message = "Invalid category")
  private String category;

  @NotBlank(message = "The message is required")
  @Size(min = 10, max = 500, message = "The message must be between 10 and 500 characters")
  private String message;
}