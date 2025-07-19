package com.moru.backend.domain.routine.domain.schedule;

public enum DayOfWeek {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    public static DayOfWeek fromJavaDay(java.time.DayOfWeek javaDayOfWeek) {
        return switch (javaDayOfWeek) {
            case MONDAY -> MON;
            case TUESDAY -> TUE;
            case WEDNESDAY -> WED;
            case THURSDAY -> THU;
            case FRIDAY -> FRI;
            case SATURDAY -> SAT;
            case SUNDAY -> SUN;
        };
    }
}
