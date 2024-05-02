package roomescape.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.dto.TimeMemberResponse;
import roomescape.dto.TimeResponse;
import roomescape.dto.TimeSaveRequest;
import roomescape.exception.IllegalTimeException;
import roomescape.mapper.TimeMapper;
import roomescape.repository.ReservationDao;
import roomescape.repository.TimeDao;

@Service
public class ReservationTimeService {

    private final TimeMapper timeMapper = new TimeMapper();
    private final TimeDao timeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(TimeDao timeDao, ReservationDao reservationDao) {
        this.timeDao = timeDao;
        this.reservationDao = reservationDao;
    }

    public List<TimeMemberResponse> findAllTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allTimes = timeDao.findAll();
        List<TimeMemberResponse> newAllTimes = new ArrayList<>();

        List<Long> bookedTimeIds = reservationDao.findTimeIdByDateThemeId(date, themeId);
        for (ReservationTime time : allTimes) {
            boolean alreadyBooked = bookedTimeIds.contains(time.getId());
            newAllTimes.add(timeMapper.mapToResponse(time, alreadyBooked));
        }
        return newAllTimes;
    }

    public List<TimeResponse> findAllTimes() {
        List<ReservationTime> reservationTimes = timeDao.findAll();
        return reservationTimes.stream()
                .map(timeMapper::mapToResponse)
                .toList();
    }

    public TimeResponse saveTime(TimeSaveRequest request) {
        ReservationTime reservationTime = timeMapper.mapToTime(request);

        if (timeDao.existByTime(reservationTime.getStartAt())) {
            throw new IllegalTimeException("[ERROR] 중복된 시간을 생성할 수 없습니다.");
        }

        Long saveId = timeDao.save(reservationTime);
        return timeMapper.mapToResponse(saveId, reservationTime);
    }

    public void deleteTimeById(Long id) {
        try {
            timeDao.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalTimeException("[ERROR] 예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
    }
}
