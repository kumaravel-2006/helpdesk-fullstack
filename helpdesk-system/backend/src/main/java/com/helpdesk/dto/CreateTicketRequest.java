package com.helpdesk.dto;

import com.helpdesk.entity.LocationType;
import com.helpdesk.entity.Priority;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateTicketRequest {

    @NotNull
    private Long subCategoryId;

    @NotNull
    private LocationType locationType;

    @NotBlank
    private String buildingOrHostelName;

    private String block;
    private String floor;

    @NotBlank
    private String roomOrArea;

    @NotBlank
    private String description;

    private Priority priority; // defaults to NORMAL if null

    private String preferredDate;
    private String preferredTimeSlot;
    private boolean permissionToEnterRoom;

    private List<String> imageUrls; // pre-uploaded object storage URLs
}
