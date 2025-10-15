package com.zeta_horizon.investment_portfolio_tracker.dto;

import com.zeta_horizon.investment_portfolio_tracker.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SupportTicketRequestDto {

    private String investmentProductName;

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 100, message = "Subject must be less than 100 characters")
    private String subject;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private Priority priority;
}
