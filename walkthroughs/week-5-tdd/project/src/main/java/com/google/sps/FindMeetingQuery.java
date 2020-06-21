// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.lang.Object;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;


public final class FindMeetingQuery {
  private static final Collection<Event> NO_EVENTS = Collections.emptySet();
  private static final Collection<String> NO_ATTENDEES = Collections.emptySet();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";

  // All dates are the first day of the year 2020.
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
  private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);

  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_60_MINUTES = 60;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;
  private static final int DURATION_2_HOUR = 120;

  private FindMeetingQuery query;
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> result = new ArrayList<>();
    result.add(TimeRange.WHOLE_DAY);

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }

    // If the event list is empty (which means everyone is available for the whole day), then return the whole day available
    if (events == null || events.isEmpty() || events.contains(NO_EVENTS)) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Copy the list of events into another list
    Collection<Event> eventResult = new ArrayList<>();
    for (Event event: events) {
        eventResult.add(event);
    }
    boolean check = false; 

    // Remove all the people who will not attend
    for (Event event: eventResult) {
        for (String attendee: event.getAttendees()) {
            if (!request.getAttendees().contains(attendee) && event.getAttendees().size() == 1) {
                check = true;
            }
        }
        // Remove the person who will not attend (if the event has more than 1 person) 
        if (check) System.out.println(eventResult.remove(event));
        // If the event list is empty (which means everyone is available for the whole day), then return the whole day available
        if (eventResult == null || eventResult.isEmpty() || eventResult.contains(NO_EVENTS)) return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    for (Event event: eventResult) {
        for (TimeRange timeRange: result) {
            if (timeRange.overlaps(event.getWhen())) {
                if (timeRange.start() < event.getWhen().start()) result.add(TimeRange.fromStartEnd(timeRange.start(), event.getWhen().start(), false));
                if (event.getWhen().end() < timeRange.end()) result.add(TimeRange.fromStartEnd(event.getWhen().end(), timeRange.end(), false));
                result.remove(timeRange);
                break;
            }
        }
    }

    // After getting all the time slots available for all the attendees, check if the time slots are longer than the meeting request's duration
    for (TimeRange timeRange: result) {
        if (timeRange.duration() < request.getDuration()) result.remove(timeRange);
        if (result == null || result.isEmpty() || result.contains(NO_EVENTS)) return Arrays.asList();
    }

    return result;
  }
}
