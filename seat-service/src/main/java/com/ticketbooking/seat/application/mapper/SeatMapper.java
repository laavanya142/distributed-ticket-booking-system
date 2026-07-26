package com.ticketbooking.seat.application.mapper;

import com.ticketbooking.seat.application.dto.CreateSingleSeatRequest;
import com.ticketbooking.seat.application.dto.SeatResponse;
import com.ticketbooking.seat.application.dto.ShowSeatDetailResponse;
import com.ticketbooking.seat.domain.entity.Seat;
import com.ticketbooking.seat.domain.entity.ShowSeat;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between domain entities and application DTOs.
 */
@Mapper(componentModel = "spring")
public interface SeatMapper {

    /**
     * Maps physical Seat entity to SeatResponse DTO.
     *
     * @param seat Seat entity.
     * @return Mapped SeatResponse DTO.
     */
    SeatResponse toSeatResponse(Seat seat);

    /**
     * Maps list of physical Seat entities to list of SeatResponse DTOs.
     *
     * @param seats List of Seat entities.
     * @return List of SeatResponse DTOs.
     */
    List<SeatResponse> toSeatResponseList(List<Seat> seats);

    /**
     * Maps ShowSeat entity with attached Seat metadata to ShowSeatDetailResponse DTO.
     *
     * @param showSeat ShowSeat entity.
     * @return Mapped ShowSeatDetailResponse DTO.
     */
    @Mapping(source = "id", target = "showSeatId")
    @Mapping(source = "seat.id", target = "seatId")
    @Mapping(source = "seat.rowNumber", target = "rowNumber")
    @Mapping(source = "seat.seatNumber", target = "seatNumber")
    @Mapping(source = "seat.category", target = "category")
    ShowSeatDetailResponse toShowSeatDetailResponse(ShowSeat showSeat);

    /**
     * Maps list of ShowSeat entities to list of ShowSeatDetailResponse DTOs.
     *
     * @param showSeats List of ShowSeat entities.
     * @return List of ShowSeatDetailResponse DTOs.
     */
    List<ShowSeatDetailResponse> toShowSeatDetailResponseList(List<ShowSeat> showSeats);

    /**
     * Maps CreateSingleSeatRequest to physical Seat domain entity.
     *
     * @param request Request payload.
     * @return Mapped Seat entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "screenId", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Seat toSeat(CreateSingleSeatRequest request);

    /**
     * Maps list of CreateSingleSeatRequest to list of physical Seat entities.
     *
     * @param requests List of CreateSingleSeatRequest payloads.
     * @return List of mapped Seat entities.
     */
    List<Seat> toSeatList(List<CreateSingleSeatRequest> requests);
}
