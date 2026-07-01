import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Slot;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.LabHours;
import kz.epam.campus.services.ScheduleService;
import kz.epam.campus.services.SlotService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingSlotService {

    @Mock
    private SlotDao slotDao;

    @Mock
    private EquipmentDao equipmentDao;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private SlotService slotService;

    private static final int EQUIPMENT_ID = 10;
    private static final int SLOT_ID = 100;

    private LocalDate date;

    @BeforeEach
    void setUp() {
        date = LocalDate.now().plusWeeks(1);
    }

    private Equipment equipment(int equipmentId) {
        Equipment equipment = new Equipment();
        equipment.setEquipmentId(equipmentId);
        equipment.setActive(true);
        return equipment;
    }

    private Slot slot(int slotId, int equipmentId, LocalDate date, LocalTime timeStart) {
        Slot slot = new Slot();
        slot.setSlotId(slotId);
        slot.setEquipmentId(equipmentId);
        slot.setDate(date);
        slot.setTimeStart(timeStart);
        return slot;
    }

    // ---------------------------------------------------------------
    // generateSlotsForDate
    // ---------------------------------------------------------------

    @Test
    void generateSlotsForDate_doesNothing_whenLabIsClosed() {
        // GIVEN
        when(scheduleService.isWorkingDay(date)).thenReturn(false);

        // WHEN
        slotService.generateSlotsForDate(date);

        // THEN
        verify(equipmentDao, never()).findAllActive();
        verify(slotDao, never()).save(any());
    }

    @Test
    void generateSlotsForDate_skipsEquipment_whenSlotsAlreadyExist() {
        // GIVEN
        when(scheduleService.isWorkingDay(date)).thenReturn(true);
        when(equipmentDao.findAllActive()).thenReturn(List.of(equipment(EQUIPMENT_ID)));
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(true);

        // WHEN
        slotService.generateSlotsForDate(date);

        // THEN
        verify(slotDao, never()).save(any());
    }

    @Test
    void generateSlotsForDate_createsHourlySlots_whenNoneExist() {
        // GIVEN
        when(scheduleService.isWorkingDay(date)).thenReturn(true);
        when(equipmentDao.findAllActive()).thenReturn(List.of(equipment(EQUIPMENT_ID)));
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(false);

        // WHEN
        slotService.generateSlotsForDate(date);

        // THEN
        ArgumentCaptor<Slot> captor = ArgumentCaptor.forClass(Slot.class);
        verify(slotDao, times(9)).save(captor.capture());
        assertEquals(LabHours.DEFAULT_START, captor.getAllValues().get(0).getTimeStart());
        assertEquals(LocalTime.of(17, 0), captor.getAllValues().get(8).getTimeStart());
    }

    @Test
    void generateSlotsForDate_generatesSlots_forEveryActiveEquipment() {
        // GIVEN
        int equipmentId2 = 20;
        when(scheduleService.isWorkingDay(date)).thenReturn(true);
        when(equipmentDao.findAllActive()).thenReturn(List.of(equipment(EQUIPMENT_ID), equipment(equipmentId2)));
        when(slotDao.existsByEquipmentIdAndDate(anyInt(), eq(date))).thenReturn(false);

        // WHEN
        slotService.generateSlotsForDate(date);

        // THEN
        verify(slotDao, times(18)).save(any(Slot.class));
    }

    // ---------------------------------------------------------------
    // getSlots
    // ---------------------------------------------------------------

    @Test
    void getSlots_returnsSlotsFromDao() {
        // GIVEN
        List<Slot> expected = List.of(slot(SLOT_ID, EQUIPMENT_ID, date, LocalTime.of(9, 0)));
        when(slotDao.findByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(expected);

        // WHEN
        List<Slot> result = slotService.getSlots(EQUIPMENT_ID, date);

        // THEN
        verify(slotDao).findByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertEquals(expected, result);
    }

    @Test
    void getSlots_returnsEmptyList_whenNoneExist() {
        // GIVEN
        when(slotDao.findByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(List.of());

        // WHEN
        List<Slot> result = slotService.getSlots(EQUIPMENT_ID, date);

        // THEN
        verify(slotDao).findByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------
    // getAvailableSlots
    // ---------------------------------------------------------------

    @Test
    void getAvailableSlots_returnsUnbookedSlotsFromDao() {
        // GIVEN
        List<Slot> expected = List.of(slot(SLOT_ID, EQUIPMENT_ID, date, LocalTime.of(9, 0)));
        when(slotDao.findUnbookedByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(expected);

        // WHEN
        List<Slot> result = slotService.getAvailableSlots(EQUIPMENT_ID, date);

        // THEN
        verify(slotDao).findUnbookedByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertEquals(expected, result);
    }

    // ---------------------------------------------------------------
    // slotsExist
    // ---------------------------------------------------------------

    @Test
    void slotsExist_returnsTrue_whenSlotsPresent() {
        // GIVEN
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(true);

        // WHEN
        boolean result = slotService.slotsExist(EQUIPMENT_ID, date);

        // THEN
        verify(slotDao).existsByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertTrue(result);
    }

    @Test
    void slotsExist_returnsFalse_whenNoSlotsPresent() {
        // GIVEN
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(false);

        // WHEN
        boolean result = slotService.slotsExist(EQUIPMENT_ID, date);

        // THEN
        verify(slotDao).existsByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertFalse(result);
    }

    // ---------------------------------------------------------------
    // getSlotById
    // ---------------------------------------------------------------

    @Test
    void getSlotById_returnsSlot_whenFound() {
        // GIVEN
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, date, LocalTime.of(9, 0))));

        // WHEN
        Slot result = slotService.getSlotById(SLOT_ID);

        // THEN
        verify(slotDao).findById(SLOT_ID);
        assertEquals(SLOT_ID, result.getSlotId());
    }

    @Test
    void getSlotById_throwsException_whenNotFound() {
        // GIVEN
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> slotService.getSlotById(SLOT_ID));

        // THEN
        verify(slotDao).findById(SLOT_ID);
        assertEquals("Slot not found", exception.getMessage());
    }
}
