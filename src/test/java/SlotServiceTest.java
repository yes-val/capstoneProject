import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Slot;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.LabHours;
import kz.epam.campus.services.ScheduleService;
import kz.epam.campus.services.impl.SlotServiceImpl;

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
public class SlotServiceTest {
    private static final int EQUIPMENT_ID = 10;
    private static final int SLOT_ID = 100;

    @Mock
    private SlotDao slotDao;

    @Mock
    private EquipmentDao equipmentDao;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private SlotServiceImpl slotService;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        date = LocalDate.now().plusWeeks(1);
    }

    @Test
    void shouldNotGenerateSlotsWhenLabIsClosed() {
        when(scheduleService.isWorkingDay(date)).thenReturn(false);

        slotService.generateSlotsForDate(date);

        verify(equipmentDao, never()).findAllActive();
        verify(slotDao, never()).save(any());
    }

    @Test
    void shouldNotGenerateWhenSlotsAlreadyExist() {
        when(scheduleService.isWorkingDay(date)).thenReturn(true);
        when(equipmentDao.findAllActive()).thenReturn(List.of(equipment(EQUIPMENT_ID)));
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(true);

        slotService.generateSlotsForDate(date);

        verify(slotDao, never()).save(any());
    }

    @Test
    void shouldGenerateSlotsForDate() {
        when(scheduleService.isWorkingDay(date)).thenReturn(true);
        when(equipmentDao.findAllActive()).thenReturn(List.of(equipment(EQUIPMENT_ID)));
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(false);

        slotService.generateSlotsForDate(date);

        ArgumentCaptor<Slot> captor = ArgumentCaptor.forClass(Slot.class);
        verify(slotDao, times(9)).save(captor.capture());
        assertEquals(LabHours.DEFAULT_START, captor.getAllValues().get(0).getTimeStart());
        assertEquals(LocalTime.of(17, 0), captor.getAllValues().get(8).getTimeStart());
    }

    @Test
    void shouldGenerateSlotsForActiveEquipment() {
        int equipmentId2 = 20;
        when(scheduleService.isWorkingDay(date)).thenReturn(true);
        when(equipmentDao.findAllActive()).thenReturn(List.of(equipment(EQUIPMENT_ID), equipment(equipmentId2)));
        when(slotDao.existsByEquipmentIdAndDate(anyInt(), eq(date))).thenReturn(false);

        slotService.generateSlotsForDate(date);

        verify(slotDao, times(18)).save(any(Slot.class));
    }

    @Test
    void shouldGetSlots() {
        List<Slot> expected = List.of(slot(SLOT_ID, EQUIPMENT_ID, date, LocalTime.of(9, 0)));
        when(slotDao.findByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(expected);

        List<Slot> result = slotService.getSlots(EQUIPMENT_ID, date);

        verify(slotDao).findByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertEquals(expected, result);
    }

    @Test
    void shouldNotReturnWhenSlotsAreAbsent() {
        when(slotDao.findByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(List.of());

        List<Slot> result = slotService.getSlots(EQUIPMENT_ID, date);

        verify(slotDao).findByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetAvailableSlots() {
        List<Slot> expected = List.of(slot(SLOT_ID, EQUIPMENT_ID, date, LocalTime.of(9, 0)));
        when(slotDao.findUnbookedByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(expected);

        List<Slot> result = slotService.getAvailableSlots(EQUIPMENT_ID, date);

        verify(slotDao).findUnbookedByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnExistingSlots() {
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(true);

        boolean result = slotService.slotsExist(EQUIPMENT_ID, date);

        verify(slotDao).existsByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertTrue(result);
    }

    @Test
    void shouldNotReturnNonexistentSlots() {
        when(slotDao.existsByEquipmentIdAndDate(EQUIPMENT_ID, date)).thenReturn(false);

        boolean result = slotService.slotsExist(EQUIPMENT_ID, date);

        verify(slotDao).existsByEquipmentIdAndDate(EQUIPMENT_ID, date);
        assertFalse(result);
    }

    @Test
    void shouldGetSlotById() {
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, date, LocalTime.of(9, 0))));

        Slot result = slotService.getSlotById(SLOT_ID);

        verify(slotDao).findById(SLOT_ID);
        assertEquals(SLOT_ID, result.getSlotId());
    }

    @Test
    void shouldNotReturnSlotByIdWhenSlotIsNotFound() {
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.empty());

        BookingException exception = assertThrows(BookingException.class,
                () -> slotService.getSlotById(SLOT_ID));

        verify(slotDao).findById(SLOT_ID);
        assertEquals("Slot not found", exception.getMessage());
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
}
