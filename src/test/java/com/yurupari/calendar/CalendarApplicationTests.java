package com.yurupari.calendar;

import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.repository.CalendarRepository;
import com.yurupari.calendar.repository.MeetingRepository;
import com.yurupari.calendar.repository.SlotRepository;
import com.yurupari.calendar.repository.UserRepository;
import com.yurupari.calendar.support.PostgreSQLTestcontainerBase;
import com.yurupari.calendar.utils.JsonTestUtils;
import com.yurupari.calendar.utils.TestEntityCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static com.yurupari.calendar.utils.TestConstants.CREATE_CALENDAR_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_CALENDAR_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_MEETING_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_MEETING_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_SLOT_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_SLOT_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_USER_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.CREATE_USER_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_CALENDAR_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_CALENDAR_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_MEETING_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_MEETING_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_SLOT_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_SLOT_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_USER_BAD_REQUEST_JSON;
import static com.yurupari.calendar.utils.TestConstants.UPDATE_USER_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CalendarApplicationTests extends PostgreSQLTestcontainerBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CalendarRepository calendarRepository;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JsonTestUtils jsonTestUtils;

	@Autowired
	private TestEntityCreator testEntityCreator;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		calendarRepository.deleteAll();
		meetingRepository.deleteAll();
		slotRepository.deleteAll();

		jdbcTemplate.execute("TRUNCATE TABLE slot, meeting, calendar, users RESTART IDENTITY CASCADE");
	}

	@Test
	void contextLoads() {
	}

	// ----- User Tests -----
	
	@Test
	void createUser_Success() throws Exception {
		var request =jsonTestUtils.loadRequest(CREATE_USER_JSON);

		mockMvc.perform(post("/api/v1/user/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("John"))
				.andExpect(jsonPath("$.lastName").value("Doe"))
				.andExpect(jsonPath("$.email").value("john.doe@example.com"))
				.andExpect(jsonPath("$.calendarId").exists());
	}

	@Test
	void createUser_BadRequest_MissingName() throws Exception {
		var request = jsonTestUtils.loadRequest(CREATE_USER_BAD_REQUEST_JSON);

		mockMvc.perform(post("/api/v1/user/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getUser_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		testEntityCreator.createTestCalendar(user);

		mockMvc.perform(get("/api/v1/user/" + user.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Test"))
				.andExpect(jsonPath("$.lastName").value("User"))
				.andExpect(jsonPath("$.email").value("test.user@example.com"))
				.andExpect(jsonPath("$.calendarId").exists());
	}

	@Test
	void getUser_NotFound() throws Exception {
		mockMvc.perform(get("/api/v1/user/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateUser_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		testEntityCreator.createTestCalendar(user);
		var request = jsonTestUtils.loadRequest(UPDATE_USER_JSON);

		mockMvc.perform(put("/api/v1/user/" + user.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(content().string("User updated successfully"));
	}

	@Test
	void updateUser_Success_EmptyEmail() throws Exception {
		var user = testEntityCreator.createTestUser();
		testEntityCreator.createTestCalendar(user);
		var request = jsonTestUtils.loadRequest(UPDATE_USER_BAD_REQUEST_JSON);

		mockMvc.perform(put("/api/v1/user/" + user.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(content().string("User updated successfully"));
	}

	@Test
	void updateUser_NotFound() throws Exception {
		var request = jsonTestUtils.loadRequest(UPDATE_USER_JSON);

		mockMvc.perform(put("/api/v1/user/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteUser_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		testEntityCreator.createTestCalendar(user);

		mockMvc.perform(delete("/api/v1/user/" + user.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteUser_NotFound() throws Exception {
		mockMvc.perform(delete("/api/v1/user/1"))
				.andExpect(status().isNotFound());
	}

	// ----- Calendar Tests -----
	
	@Test
	void createCalendar_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var request = jsonTestUtils.loadRequest(CREATE_CALENDAR_JSON).replace("\"userId\": 1", "\"userId\": " + user.getId());

		mockMvc.perform(post("/api/v1/calendar/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.timezone").value("America/New_York"))
				.andExpect(jsonPath("$.userId").value(user.getId()));
	}

	@Test
	void createCalendar_BadRequest_MissingTimezone() throws Exception {
		var user = testEntityCreator.createTestUser();
		var request = jsonTestUtils.loadRequest(CREATE_CALENDAR_BAD_REQUEST_JSON).replace("\"userId\": 1", "\"userId\": " + user.getId());

		mockMvc.perform(post("/api/v1/calendar/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getCalendar_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);

		mockMvc.perform(get("/api/v1/calendar/" + calendar.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(calendar.getId()))
				.andExpect(jsonPath("$.timezone").value("America/New_York"))
				.andExpect(jsonPath("$.userId").value(user.getId()));
	}

	@Test
	void getCalendar_NotFound() throws Exception {
		mockMvc.perform(get("/api/v1/calendar/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateCalendar_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var request = jsonTestUtils.loadRequest(UPDATE_CALENDAR_JSON);

		mockMvc.perform(put("/api/v1/calendar/" + calendar.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(content().string("Calendar updated successfully"));
	}

	@Test
	void updateCalendar_BadRequest_EmptyTimezone() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var request = jsonTestUtils.loadRequest(UPDATE_CALENDAR_BAD_REQUEST_JSON);

		mockMvc.perform(put("/api/v1/calendar/" + calendar.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateCalendar_NotFound() throws Exception {
		var request = jsonTestUtils.loadRequest(UPDATE_CALENDAR_JSON);

		mockMvc.perform(put("/api/v1/calendar/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound());
	}

	// ----- Meeting Tests -----
	
	@Test
	void createMeeting_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var request = jsonTestUtils.loadRequest(CREATE_MEETING_JSON).replace("\"hostId\": 1", "\"hostId\": " + user.getId());

		mockMvc.perform(post("/api/v1/meeting/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.hostId").value(user.getId()))
				.andExpect(jsonPath("$.title").value("Project Sync"));
	}

	@Test
	void createMeeting_BadRequest_MissingTitle() throws Exception {
		var user = testEntityCreator.createTestUser();
		var request = jsonTestUtils.loadRequest(CREATE_MEETING_BAD_REQUEST_JSON).replace("\"hostId\": 1", "\"hostId\": " + user.getId());

		mockMvc.perform(post("/api/v1/meeting/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getMeeting_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var meeting = testEntityCreator.createTestMeeting(user);

		mockMvc.perform(get("/api/v1/meeting/" + meeting.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(meeting.getId()))
				.andExpect(jsonPath("$.hostId").value(user.getId()))
				.andExpect(jsonPath("$.title").value("Test Meeting"));
	}

	@Test
	void getMeeting_NotFound() throws Exception {
		mockMvc.perform(get("/api/v1/meeting/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateMeeting_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var meeting = testEntityCreator.createTestMeeting(user);
		var request = jsonTestUtils.loadRequest(UPDATE_MEETING_JSON);

		mockMvc.perform(put("/api/v1/meeting/" + meeting.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(content().string("Meeting updated successfully"));
	}

	@Test
	void updateMeeting_BadRequest_EmptyTitle() throws Exception {
		var user = testEntityCreator.createTestUser();
		var meeting = testEntityCreator.createTestMeeting(user);
		var request = jsonTestUtils.loadRequest(UPDATE_MEETING_BAD_REQUEST_JSON);

		mockMvc.perform(put("/api/v1/meeting/" + meeting.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateMeeting_NotFound() throws Exception {
		var request = jsonTestUtils.loadRequest(UPDATE_MEETING_JSON);

		mockMvc.perform(put("/api/v1/meeting/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteMeeting_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var meeting = testEntityCreator.createTestMeeting(user);

		mockMvc.perform(delete("/api/v1/meeting/" + meeting.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteMeeting_NotFound() throws Exception {
		mockMvc.perform(delete("/api/v1/meeting/1"))
				.andExpect(status().isNotFound());
	}

	// ----- Slot Tests -----
	
	@Test
	void createSlot_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var request = jsonTestUtils.loadRequest(CREATE_SLOT_JSON)
				.replace("\"userId\": 1", "\"userId\": " + user.getId());

		mockMvc.perform(post("/api/v1/slot/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.calendarId").value(calendar.getId()))
				.andExpect(jsonPath("$.startTime").value("2024-01-01T09:00:00"))
				.andExpect(jsonPath("$.endTime").value("2024-01-01T10:00:00"))
				.andExpect(jsonPath("$.status").value("FREE"))
				.andExpect(jsonPath("$.role").doesNotExist());
	}

	@Test
	void createSlot_BadRequest_MissingEndTime() throws Exception {
		var user = testEntityCreator.createTestUser();
		testEntityCreator.createTestCalendar(user);
		var request = jsonTestUtils.loadRequest(CREATE_SLOT_BAD_REQUEST_JSON)
				.replace("\"userId\": 1", "\"userId\": " + user.getId());

		mockMvc.perform(post("/api/v1/slot/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getSlot_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var meeting = testEntityCreator.createTestMeeting(user);
		var slot = testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-01T09:00:00Z"), Instant.parse("2024-01-01T10:00:00Z"), SlotStatus.FREE, ParticipantRole.HOST);

		mockMvc.perform(get("/api/v1/slot/" + slot.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(slot.getId()))
				.andExpect(jsonPath("$.calendarId").value(calendar.getId()))
				.andExpect(jsonPath("$.meetingId").value(meeting.getId()))
				.andExpect(jsonPath("$.startTime").value("2024-01-01T04:00:00"))
				.andExpect(jsonPath("$.endTime").value("2024-01-01T05:00:00"))
				.andExpect(jsonPath("$.status").value("FREE"))
				.andExpect(jsonPath("$.role").value("HOST"));
	}

	@Test
	void getSlots_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var meeting = testEntityCreator.createTestMeeting(user);

		testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-01T09:00:00Z"), Instant.parse("2024-01-01T10:00:00Z"), SlotStatus.FREE, ParticipantRole.HOST);
		testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-01T11:00:00Z"), Instant.parse("2024-01-01T12:00:00Z"), SlotStatus.BUSY, ParticipantRole.INVITEE);
		testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-02T09:00:00Z"), Instant.parse("2024-01-02T10:00:00Z"), SlotStatus.FREE, ParticipantRole.HOST);

		var from = "2024-01-01T00:00:00";
		var until = "2024-01-01T23:59:59";

		mockMvc.perform(get("/api/v1/slot/user/" + user.getId())
						.param("from", from)
						.param("until", until))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].calendarId").value(calendar.getId()))
				.andExpect(jsonPath("$[0].startTime").value("2024-01-01T04:00:00"))
				.andExpect(jsonPath("$[1].calendarId").value(calendar.getId()))
				.andExpect(jsonPath("$[1].startTime").value("2024-01-01T06:00:00"));
	}

	@Test
	void updateSlot_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var meeting = testEntityCreator.createTestMeeting(user);
		var slot = testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-01T09:00:00Z"), Instant.parse("2024-01-01T10:00:00Z"), SlotStatus.FREE, ParticipantRole.HOST);
		var request = jsonTestUtils.loadRequest(UPDATE_SLOT_JSON);

		mockMvc.perform(put("/api/v1/slot/" + slot.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(content().string("Slot updated successfully"));
	}

	@Test
	void updateSlot_BadRequest_InvalidTimeRange() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var meeting = testEntityCreator.createTestMeeting(user);
		var slot = testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-01T09:00:00Z"), Instant.parse("2024-01-01T10:00:00Z"), SlotStatus.FREE, ParticipantRole.HOST);
		var request = jsonTestUtils.loadRequest(UPDATE_SLOT_BAD_REQUEST_JSON);

		mockMvc.perform(put("/api/v1/slot/" + slot.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getSlot_NotFound() throws Exception {
		mockMvc.perform(get("/api/v1/slot/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateSlot_NotFound() throws Exception {
		var request = jsonTestUtils.loadRequest(UPDATE_SLOT_JSON);

		mockMvc.perform(put("/api/v1/slot/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteSlot_Success() throws Exception {
		var user = testEntityCreator.createTestUser();
		var calendar = testEntityCreator.createTestCalendar(user);
		var meeting = testEntityCreator.createTestMeeting(user);
		var slot = testEntityCreator.createTestSlot(calendar, meeting, Instant.parse("2024-01-01T09:00:00Z"), Instant.parse("2024-01-01T10:00:00Z"), SlotStatus.FREE, ParticipantRole.HOST);

		mockMvc.perform(delete("/api/v1/slot/" + slot.getId()))
				.andExpect(status().isNoContent());
	}
}
