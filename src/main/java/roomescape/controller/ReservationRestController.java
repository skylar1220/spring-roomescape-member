package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.dto.AdminReservationCreateRequest;
import roomescape.dto.ReservationCreateRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getAll() {
        List<ReservationResponse> responses = reservationService.findAll();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request,
                                                      Member member) {
        ReservationResponse reservationResponse = reservationService.save(request, member);

        URI location = URI.create("/reservations/" + reservationResponse.id());
        return ResponseEntity.created(location).body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> adminCreate(
            @Valid @RequestBody AdminReservationCreateRequest adminReservationCreateRequest) {
        ReservationResponse reservationResponse = reservationService.save(adminReservationCreateRequest);

        URI location = URI.create("/admin/reservations/" + reservationResponse.id());
        return ResponseEntity.created(location).body(reservationResponse);
    }

    @GetMapping("/admin/reservations")  // 얘를 Requestbody로 못받으려나
    public ResponseEntity<List<ReservationResponse>> getAll(@RequestParam Long memberId, @RequestParam Long themeId, @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo) {
        List<ReservationResponse> reservationResponses = reservationService.findByMemberIdAndThemeIdAndDateFromTo(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(reservationResponses);
    }
}
