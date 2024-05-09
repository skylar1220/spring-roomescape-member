package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.AdminReservationCreateRequest;
import roomescape.dto.ReservationCreateRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.ExistReservationException;
import roomescape.exception.IllegalReservationException;
import roomescape.repository.MemberDao;
import roomescape.repository.ReservationDao;
import roomescape.repository.ThemeDao;
import roomescape.repository.TimeDao;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao; // TODO: 이것까지 갖고있는 게 맞을까?

    public ReservationService(ReservationDao reservationDao, final TimeDao timeDao, ThemeDao themeDao,
                              final MemberDao memberDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationDao.findAll();
        return ReservationResponse.fromReservations(reservations);
    }

    public List<ReservationResponse> findByMemberIdAndThemeIdAndDateFromTo(final Long memberId, final Long themeId,
                                                                           final LocalDate dateFrom,
                                                                           final LocalDate dateTo) {
       return ReservationResponse.fromReservations(reservationDao.findByMemberIdAndThemeIdAndDateFromTo(memberId, themeId, dateFrom, dateTo));
    }

    public ReservationResponse save(final AdminReservationCreateRequest request) {
        if (request.date().isBefore(LocalDate.now())) {
            throw new IllegalReservationException("[ERROR] 과거 날짜는 예약할 수 없습니다.");
        }

        ReservationTime time = timeDao.findById(request.timeId());
        if (request.date().isEqual(LocalDate.now()) && time.isPast()) {
            throw new IllegalReservationException("[ERROR] 과거 시간은 예약할 수 없습니다.");
        }

        Theme theme = themeDao.findById(request.themeId());
        Member member = memberDao.findById(request.memberId());
        Reservation reservation = ReservationCreateRequest.toReservation(member.getName(), request.date(), time, theme, member);

        if (reservationDao.existByDateAndTimeAndTheme(reservation.getDate(), reservation.getTimeId(),
                reservation.getThemeId())) {
            throw new ExistReservationException("[ERROR] 같은 날짜, 테마, 시간에 중복된 예약을 생성할 수 없습니다.");
        }

        return ReservationResponse.fromReservation(reservationDao.save(reservation));
    }

    public ReservationResponse save(ReservationCreateRequest request, final Member member) { // TODO: 중복제거
        String memberName = member.getName();

        if (request.date().isBefore(LocalDate.now())) {
            throw new IllegalReservationException("[ERROR] 과거 날짜는 예약할 수 없습니다.");
        }

        ReservationTime time = timeDao.findById(request.timeId());
        if (request.date().isEqual(LocalDate.now()) && time.isPast()) {
            throw new IllegalReservationException("[ERROR] 과거 시간은 예약할 수 없습니다.");
        }

        Theme theme = themeDao.findById(request.themeId());
        Reservation reservation = ReservationCreateRequest.toReservation(memberName, request.date(), time, theme,
                member);

        if (reservationDao.existByDateAndTimeAndTheme(reservation.getDate(), reservation.getTimeId(),
                reservation.getThemeId())) {
            throw new ExistReservationException("[ERROR] 같은 날짜, 테마, 시간에 중복된 예약을 생성할 수 없습니다.");
        }

        return ReservationResponse.fromReservation(reservationDao.save(reservation));
    }

    public void deleteById(Long id) {
        reservationDao.deleteById(id);
    }
}
