package com.ticketbooking.booking.interfaces.rest.mapper;

import com.ticketbooking.booking.application.model.CreateBookingCommand;
import com.ticketbooking.booking.domain.entity.Booking;
import com.ticketbooking.booking.domain.entity.BookingItem;
import com.ticketbooking.booking.interfaces.rest.dto.BookingItemResponse;
import com.ticketbooking.booking.interfaces.rest.dto.BookingResponse;
import com.ticketbooking.booking.interfaces.rest.dto.CreateBookingRequest;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between REST DTOs, domain models, and entities.
 */
@Mapper(componentModel = "spring")
public interface BookingMapper {

    /**
     * Maps CreateBookingRequest, authenticated userId, and idempotencyKey to CreateBookingCommand.
     *
     * @param request Client request DTO.
     * @param userId Authenticated user ID.
     * @param idempotencyKey Client idempotency key.
     * @return CreateBookingCommand object.
     */
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    CreateBookingCommand toCommand(CreateBookingRequest request, UUID userId, String idempotencyKey);

    /**
     * Maps a Booking aggregate entity to a BookingResponse payload.
     *
     * @param booking Booking entity.
     * @return BookingResponse DTO.
     */
    BookingResponse toBookingResponse(Booking booking);

    /**
     * Maps a BookingItem entity to a BookingItemResponse DTO.
     *
     * @param item BookingItem entity.
     * @return BookingItemResponse DTO.
     */
    BookingItemResponse toBookingItemResponse(BookingItem item);

    /**
     * Maps a list of BookingItem entities to BookingItemResponse DTOs.
     *
     * @param items List of BookingItem entities.
     * @return List of BookingItemResponse DTOs.
     */
    List<BookingItemResponse> toBookingItemResponseList(List<BookingItem> items);
}
