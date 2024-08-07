package org.arabius.platform.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.arabius.platform.domain.Guide;
import org.arabius.platform.domain.GuideAbsence;
import org.arabius.platform.domain.GuideSlot;
import org.arabius.platform.domain.Lesson;
import org.arabius.platform.domain.Room;
import org.arabius.platform.domain.RoomPriority;
import org.arabius.platform.domain.Timeslot;
import org.arabius.platform.domain.Timetable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DemoDataResource {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/arabius_prod";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public static Timetable buildTimetable() {
        List<Room> rooms = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();
        List<Guide> guides = new ArrayList<>();
        List<GuideSlot> guideSlots = new ArrayList<>();
        List<RoomPriority> roomPriorities = new ArrayList<>();
        List<GuideAbsence> guideAbsences = new ArrayList<>();

        String startDateString = "2024-08-05";
        String endDateString = "2024-08-10";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

            // Load rooms from database
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setId(rs.getInt("id"));
                    room.setName(rs.getString("name"));
                    room.setCapacity(rs.getInt("capacity"));
                    room.setBranchId(rs.getInt("branch_id"));
                    rooms.add(room);
                }
            } catch (SQLException ex) {
                System.out.println("Error loading rooms from database: " + ex.getMessage());
            }

            // Load lessons from database
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT * FROM automated_scheduling_lessons_view where date between '" + startDateString
                                    + "' and '" + endDateString
                                    + "' and status not in ('cancelled', 'in-progress') order by date, start")) {
                while (rs.next()) {
                    Lesson lesson = new Lesson();
                    lesson.setId(rs.getInt("id"));
                    lesson.setLevel(rs.getInt("level"));
                    lesson.setStatus(rs.getString("status"));
                    lesson.setStudentCount(rs.getInt("student_count"));
                    lesson.setDate(rs.getDate("date").toLocalDate());
                    lesson.setStart(rs.getTime("start").toLocalTime());
                    lesson.setEnd(rs.getTime("end").toLocalTime());
                    lesson.setStudentGroupHash(rs.getString("student_group_hash"));
                    lesson.setSlotId(rs.getInt("slot_id"));
                    lesson.setBranchId(rs.getInt("branch_id"));
                    lesson.setLessonTypeId(rs.getInt("lesson_type_id"));
                    lesson.setBufferStart(rs.getTimestamp("buffer_start").toLocalDateTime());
                    lesson.setBufferEnd(rs.getTimestamp("buffer_end").toLocalDateTime());
                    lesson.setAllowGuide(rs.getBoolean("allow_guide"));
                    lesson.setAllowRoom(rs.getBoolean("allow_room"));
                    lesson.setAllowVideoCall(rs.getBoolean("allow_video_call"));
                    lesson.setEnforceRoomCapacity(rs.getBoolean("enforce_room_capacity_limits"));
                    lesson.setRequireGuide(rs.getBoolean("require_guide"));
                    lesson.setRequireRoom(rs.getBoolean("require_room"));
                    lesson.setLevelIsDifficult(rs.getBoolean("is_difficult"));
                    lesson.setGuideStickiness(rs.getInt("guide_stickiness"));
                    if (!lesson.getStatus().equals("scheduled")) {
                        lesson.setInitialRoomId(rs.getInt("room_id"));
                        lesson.setInitialGuideId(rs.getInt("guide_id"));
                    }
                    lessons.add(lesson);
                }
            } catch (SQLException ex) {
                System.out.println("Error loading lessons from database: " + ex.getMessage());
            }

            // Load guides from database
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT users.id,users.guide_scheduling_cost,SUBSTRING_INDEX(users.name,'|',1)AS name,GROUP_CONCAT(DISTINCT levels.id SEPARATOR'|')AS levels FROM users,guide_level,levels,model_has_roles,roles WHERE 1=1 AND users.id=guide_level.user_id AND levels.id=guide_level.level_id AND guide_level.admin_rating<>0 AND users.active=1 AND users.id=model_has_roles.model_id AND model_has_roles.model_type=\"App\\\\Models\\\\User\" AND model_has_roles.role_id=roles.id AND roles.type='guide' GROUP BY users.id")) {
                while (rs.next()) {
                    Guide guide = new Guide();
                    guide.setId(rs.getInt("id"));
                    guide.setName(rs.getString("name"));
                    guide.setLevels(rs.getString("levels"));
                    guide.setSchedulingCost(rs.getInt("guide_scheduling_cost"));
                    guides.add(guide);
                }
            } catch (SQLException ex) {
                System.out.println("Error loading guides from database: " + ex.getMessage());
            }

            // Load guide slots from database
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT * from automated_scheduling_guide_shifts_by_day where date between '"
                                    + startDateString + "' and '" + endDateString + "'")) {
                while (rs.next()) {
                    GuideSlot guideSlot = new GuideSlot();
                    guideSlot.setDate(rs.getDate("date").toLocalDate());
                    guideSlot.setGuideId(rs.getInt("guide_id"));
                    guideSlot.setSlotIds(rs.getString("slot_ids"));
                    guideSlot.setStartTime(rs.getTime("start_time").toLocalTime());
                    guideSlot.setEndTime(rs.getTime("end_time").toLocalTime());
                    guideSlots.add(guideSlot);
                }
            } catch (SQLException ex) {
                System.out.println("Error loading guides slots from database: " + ex.getMessage());
            }

            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT room_room_group.room_id,room_room_group.`order` AS priority,lesson_types.id as lesson_type_id FROM room_groups,room_room_group,lesson_types WHERE room_room_group.room_group_id=room_groups.id and lesson_types.room_group_id=room_groups.id")) {
                while (rs.next()) {
                    RoomPriority roomPriority = new RoomPriority();
                    roomPriority.setRoomId(rs.getInt("room_id"));
                    roomPriority.setPriority(rs.getInt("priority"));
                    roomPriority.setLessonTypeId(rs.getInt("lesson_type_id"));
                    roomPriorities.add(roomPriority);
                }
            } catch (SQLException ex) {
                System.out.println("Error loading room priorities from database: " + ex.getMessage());
            }
            // load guide absences
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM guide_absences where start < '" + endDateString
                            + "' and end > '" + startDateString + "'")) {
                while (rs.next()) {
                    GuideAbsence guideAbsence = new GuideAbsence();
                    guideAbsence.setId(rs.getInt("id"));
                    guideAbsence.setGuideId(rs.getInt("user_id"));
                    guideAbsence.setStartDateTime(rs.getTimestamp("start").toLocalDateTime());
                    guideAbsence.setEndDateTime(rs.getTimestamp("end").toLocalDateTime());
                    guideAbsences.add(guideAbsence);
                }
            } catch (SQLException ex) {
                System.out.println("Error loading guide absences from database: " + ex.getMessage());
            }

        } catch (SQLException ex) {
            System.out.println("Error connecting to database: " + ex.getMessage());
        }

        for (RoomPriority roomPriority : roomPriorities) {
            for (Room room : rooms) {
                if (roomPriority.getRoomId() == room.getId()) {
                    room.addRoomPriority(roomPriority);
                    break;
                }
            }
        }

        List<Timeslot> timeSlots = new ArrayList<>();

        for (Lesson lesson : lessons) {
            LocalDate date = lesson.getDate();
            LocalTime startTime = lesson.getStart();
            LocalTime endTime = lesson.getEnd();
            Integer slotId = lesson.getSlotId();

            if (slotId != null) {
                Timeslot timeSlot = new Timeslot(slotId, date, startTime, endTime);
                if (!timeSlots.contains(timeSlot)) {
                    timeSlots.add(timeSlot);
                }
            }

            if (lesson.getInitialRoomId() != null) {
                for (Room room : rooms) {
                    if (Integer.valueOf(room.getId()).equals(lesson.getInitialRoomId())) {
                        lesson.setRoom(room);
                        break;
                    }
                }
            }

            if (lesson.getInitialGuideId() != null) {
                for (Guide guide : guides) {
                    if (Integer.valueOf(guide.getId()).equals(lesson.getInitialGuideId())) {
                        lesson.setGuide(guide);
                        break;
                    }
                }
            }
        }

        for (Guide guide : guides) {
            guide.addMatchingGuideslots(guideSlots);
            guide.addMatchingGuideAbsences(guideAbsences);
            // get last lesson from lessons
            // Lesson lesson = lessons.get(lessons.size() - 1);
            // System.out.println("Guide: " + guide.getName() + " has " +
            // guide.guideHasSlotOnDay(lesson.getDate(), lesson.getSlotId()) +
            // lesson.getDate() + " " + lesson.getStart());

            // System.out.println("Guide: " + guide.getName() + " has " +
            // guide.getGuideSlots().size() + " guide slots");
            // System.out.println("Guide: " + guide.getName() + " has " +
            // guide.getGuideAbsences().size() + " guide absences");
            // System.out.println("Guide: " + guide.getName() + " has " +
            // guide.getSchedulingCost() + " scheduling cost");
        }

        // Now you can use the timeSlots list for further processing or manipulation

        lessons.removeIf(lesson -> "ended".equals(lesson.getStatus()));

        // remove all b X number of lessons
        lessons = lessons.subList(0, 100);

        return new Timetable("Demo_data", rooms, lessons, guides, timeSlots);
    }
}
