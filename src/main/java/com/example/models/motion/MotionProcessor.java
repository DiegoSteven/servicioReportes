package com.example.models.motion;

import com.example.models.ModelosBases.Event;
import com.example.models.ModelosBases.Position;
import com.example.Util.TripsConfig;

public class MotionProcessor {

    public static void updateState(MotionState state, Position position, boolean motion, TripsConfig config) {

        state.setEvent(null); // limpia evento anterior

        boolean oldState = state.getMotionState();

        if (oldState == motion) {
            if (state.getMotionTime() != null) {
                long duration = position.getFixTime().getTime() - state.getMotionTime().getTime();
                double distance = position.getTotalDistance() - state.getMotionDistance();

                Boolean ignition = null;
                if (config.getUseIgnition() && position.getAttributes().containsKey("ignition")) {
                    ignition = (Boolean) position.getAttributes().get("ignition");
                }

                boolean generateEvent = false;

                if (motion) {
                    if (duration >= config.getMinimalTripDuration() || distance >= config.getMinimalTripDistance()) {
                        generateEvent = true;
                    }
                } else {
                    if (duration >= config.getMinimalParkingDuration()
                            || (ignition != null && !ignition)) {
                        generateEvent = true;
                    }
                }

                if (generateEvent) {
                    String eventType = motion ? Event.TYPE_DEVICE_MOVING : Event.TYPE_DEVICE_STOPPED;
                    state.setMotionStreak(motion);
                    state.setMotionTime(null);
                    state.setMotionDistance(0);
                    state.setEvent(eventType); // <- solo guarda el tipo
                }
            }

        } else {
            // cambio de estado de motion
            state.setMotionState(motion);

            if (state.getMotionStreak() == motion) {
                // ya venía así antes
                state.setMotionTime(null);
                state.setMotionDistance(0);
            } else {
                // nuevo streak
                state.setMotionTime(position.getFixTime());
                state.setMotionDistance(position.getTotalDistance());
            }
        }
    }
}
