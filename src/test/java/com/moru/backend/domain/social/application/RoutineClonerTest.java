package com.moru.backend.domain.social.application;

import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.*;
import com.moru.backend.domain.routine.dao.routine.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutineClonerTest {

    @InjectMocks
    private RoutineCloner routineCloner;

    @Mock private RoutineRepository routineRepository;
    @Mock private RoutineStepRepository routineStepRepository;
    @Mock private RoutineTagRepository routineTagRepository;
    @Mock private RoutineAppRepository routineAppRepository;
    @Mock private RoutineScheduleRepository routineScheduleRepository;

    private Routine origin;
    private User owner;

    @BeforeEach
    void setup() {
        owner = User.builder()
                .id(UUID.randomUUID())
                .nickname("tester")
                .build();

        RoutineStep step = RoutineStep.builder()
                .stepOrder(1)
                .name("준비운동")
                .estimatedTime(Duration.ofMinutes(5))
                .build();

        RoutineTag rt = RoutineTag.builder()
                .tag(Tag.builder().id(UUID.randomUUID()).name("건강").build())
                .build();

        RoutineApp ra = RoutineApp.builder()
                .app(App.builder().id(UUID.randomUUID()).name("Stretch App").build())
                .build();

        origin = Routine.builder()
                .id(UUID.randomUUID())
                .title("Test Routine")
                .content("Test Content")
                .isSimple(false)
                .isUserVisible(true)
                .requiredTime(Duration.ofMinutes(30))
                .imageUrl("image.png")
                .user(User.builder().id(UUID.randomUUID()).build())
                .routineSteps(List.of(step))
                .routineTags(List.of(rt))
                .routineApps(List.of(ra))
                .build();

    }

    @Test
    void 루틴을_복사하면_모든_구성요소가_복사된다() {
        // given
        RoutineSchedule schedule = RoutineSchedule.builder()
                .dayOfWeek(DayOfWeek.MON)
                .time(LocalTime.of(8, 30))
                .alarmEnabled(true)
                .build();

        when(routineScheduleRepository.findAllByRoutineId(origin.getId()))
                .thenReturn(List.of(schedule));

        when(routineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(routineStepRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(routineTagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(routineAppRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(routineScheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        Routine copy = routineCloner.cloneRoutine(origin, owner);

        // then
        assertThat(copy.getTitle()).isEqualTo(origin.getTitle());
        assertThat(copy.getUser()).isEqualTo(owner);
        assertThat(copy.getRequiredTime()).isEqualTo(origin.getRequiredTime());
        assertThat(copy.isUserVisible()).isTrue();

        verify(routineStepRepository).save(any());
        verify(routineTagRepository).save(any());
        verify(routineAppRepository).save(any());
        verify(routineScheduleRepository).save(any());
    }
}
